package com.example.data.repository

import com.example.data.local.DefaultChinaHolidays
import com.example.data.local.HolidayDao
import com.example.data.local.PreferencesRepository
import com.example.data.model.ChinaDayType
import com.example.data.model.HolidayDayEntity
import com.example.data.model.SimpleDate
import com.example.data.remote.HolidayApiService
import com.example.data.remote.HolidayCnResponse
import com.example.data.remote.HolidayNetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

sealed interface HolidaySyncResult {
    data class Success(
        val syncedYears: List<Int>,
        val entriesCount: Int,
        val sourceName: String
    ) : HolidaySyncResult

    data class Failure(val message: String) : HolidaySyncResult
}

class HolidayRepository(
    private val holidayDao: HolidayDao,
    private val preferencesRepository: PreferencesRepository,
    private val apiService: HolidayApiService = HolidayNetworkClient.apiService
) {

    val allHolidaysFlow: Flow<List<HolidayDayEntity>> = holidayDao.observeAllHolidays()

    val holidayMapFlow: Flow<Map<String, HolidayDayEntity>> =
        allHolidaysFlow.map { list -> list.associateBy { it.dateIso } }

    suspend fun getHolidayMapOnce(): Map<String, HolidayDayEntity> =
        holidayDao.getAllHolidaysOnce().associateBy { it.dateIso }

    suspend fun ensureSeeded() {
        if (holidayDao.countHolidays() == 0) {
            holidayDao.upsertAll(DefaultChinaHolidays.allBaselineHolidays())
        }
    }

    suspend fun syncIfDue(): HolidaySyncResult? {
        val prefs = preferencesRepository.preferencesFlow.first()
        val intervalHours = prefs.autoSyncInterval.intervalHours
        if (intervalHours == Long.MAX_VALUE) return null
        val elapsedHours = (System.currentTimeMillis() - prefs.lastSyncMillis) / (1000L * 60L * 60L)
        return if (prefs.lastSyncMillis == 0L || elapsedHours >= intervalHours) {
            val currentYear = SimpleDate.today().year
            syncYearsFromNetwork(listOf(currentYear, currentYear + 1))
        } else {
            null
        }
    }

    /**
     * Fetches official China Mainland statutory holiday & lieu-workday JSON from live mirrors
     * with automatic multi-mirror failover, preserving user manual overrides.
     */
    suspend fun syncYearsFromNetwork(years: List<Int>): HolidaySyncResult = withContext(Dispatchers.IO) {
        val userOverrideDates = holidayDao.getUserOverrideDates().toSet()
        val syncedYears = mutableListOf<Int>()
        var totalEntries = 0
        var lastSource = ""
        var lastError: String? = null

        for (year in years.distinct().sorted()) {
            val fetched = fetchSingleYearWithFailover(year)
            if (fetched != null) {
                val (sourceName, entries) = fetched
                val filtered = entries.filter { it.dateIso !in userOverrideDates }
                holidayDao.deleteNonOverridesForYear(year)
                if (filtered.isNotEmpty()) {
                    holidayDao.upsertAll(filtered)
                }
                syncedYears.add(year)
                totalEntries += filtered.size
                lastSource = sourceName
            } else {
                lastError = "${year}年官方假期数据暂未发布或网络不可达"
            }
        }

        if (syncedYears.isNotEmpty()) {
            val yearsLabel = syncedYears.joinToString(", ")
            preferencesRepository.recordSyncSuccess(
                source = lastSource,
                yearsLabel = yearsLabel
            )
            HolidaySyncResult.Success(
                syncedYears = syncedYears,
                entriesCount = totalEntries,
                sourceName = lastSource
            )
        } else {
            HolidaySyncResult.Failure(
                lastError ?: "网络连接失败，当前已自动保持内置及本地缓存假期安排"
            )
        }
    }

    private suspend fun fetchSingleYearWithFailover(year: Int): Pair<String, List<HolidayDayEntity>>? {
        // Mirror 1: jsDelivr CDN (holiday-cn State Council repository, fast in Mainland China)
        runCatching {
            val response = apiService.fetchHolidayCn(HolidayNetworkClient.jsDelivrUrl(year))
            if (response.days.isNotEmpty()) {
                return "国务院节假日镜像 (jsDelivr)" to response.days.map { day ->
                    HolidayDayEntity(
                        dateIso = day.date,
                        year = day.date.take(4).toIntOrNull() ?: year,
                        name = if (day.isOffDay) day.name else "${day.name}调休补班",
                        isOffDay = day.isOffDay,
                        isUserOverride = false,
                        source = "jsdelivr"
                    )
                }
            }
        }

        // Mirror 2: GitHub Raw (holiday-cn)
        runCatching {
            val response = apiService.fetchHolidayCn(HolidayNetworkClient.githubRawUrl(year))
            if (response.days.isNotEmpty()) {
                return "国务院节假日源 (GitHub)" to response.days.map { day ->
                    HolidayDayEntity(
                        dateIso = day.date,
                        year = day.date.take(4).toIntOrNull() ?: year,
                        name = if (day.isOffDay) day.name else "${day.name}调休补班",
                        isOffDay = day.isOffDay,
                        isUserOverride = false,
                        source = "github"
                    )
                }
            }
        }

        // Mirror 3: Timor.tech China Holiday API
        runCatching {
            val response = apiService.fetchTimor(HolidayNetworkClient.timorUrl(year))
            val map = response.holiday
            if (response.code == 0 && !map.isNullOrEmpty()) {
                val list = map.values.mapNotNull { item ->
                    val iso = item.date.takeIf { it.length == 10 } ?: return@mapNotNull null
                    HolidayDayEntity(
                        dateIso = iso,
                        year = iso.take(4).toIntOrNull() ?: year,
                        name = item.name.ifBlank { if (item.holiday) "法定节假日" else "调休补班" },
                        isOffDay = item.holiday,
                        isUserOverride = false,
                        source = "timor"
                    )
                }
                if (list.isNotEmpty()) {
                    return "Timor 节假日接口" to list
                }
            }
        }

        return null
    }

    suspend fun setManualOverride(
        date: SimpleDate,
        dayType: ChinaDayType,
        customLabel: String
    ) {
        val label = customLabel.trim().ifBlank { dayType.displayName }
        val entity = HolidayDayEntity(
            dateIso = date.toIsoString(),
            year = date.year,
            name = label,
            isOffDay = dayType == ChinaDayType.STATUTORY_HOLIDAY || dayType == ChinaDayType.WEEKEND,
            overrideType = dayType.name,
            isUserOverride = true,
            source = "manual",
            updatedAtMillis = System.currentTimeMillis()
        )
        holidayDao.upsert(entity)
    }

    suspend fun setManualOverrideRange(
        startDate: SimpleDate,
        endDate: SimpleDate,
        dayType: ChinaDayType,
        customLabel: String
    ): Int {
        val actualStart = if (startDate <= endDate) startDate else endDate
        val actualEnd = if (startDate <= endDate) endDate else startDate
        val totalDays = (actualStart.daysUntil(actualEnd) + 1).coerceAtMost(31)
        val entities = (0 until totalDays).map { offset ->
            val d = actualStart.plusDays(offset)
            HolidayDayEntity(
                dateIso = d.toIsoString(),
                year = d.year,
                name = customLabel.trim().ifBlank { dayType.displayName },
                isOffDay = dayType == ChinaDayType.STATUTORY_HOLIDAY || dayType == ChinaDayType.WEEKEND,
                overrideType = dayType.name,
                isUserOverride = true,
                source = "manual",
                updatedAtMillis = System.currentTimeMillis()
            )
        }
        holidayDao.upsertAll(entities)
        return entities.size
    }

    suspend fun revertManualOverride(date: SimpleDate) {
        val iso = date.toIsoString()
        val builtin = DefaultChinaHolidays.allBaselineHolidays().firstOrNull { it.dateIso == iso }
        if (builtin != null) {
            holidayDao.upsert(builtin)
        } else {
            holidayDao.deleteByDate(iso)
        }
    }

    suspend fun resetAllOverridesToOfficial() {
        holidayDao.clearAllUserOverrides()
        val existing = holidayDao.getAllHolidaysOnce().map { it.dateIso }.toSet()
        val missingBuiltins = DefaultChinaHolidays.allBaselineHolidays().filter { it.dateIso !in existing }
        if (missingBuiltins.isNotEmpty()) {
            holidayDao.upsertAll(missingBuiltins)
        }
    }

    /**
     * Parses manually pasted holiday JSON (holiday-cn format) or plain text lines:
     * Example line format:
     *   2027-01-01 休 元旦
     *   2027-02-14 班 春节补班
     */
    suspend fun importManualScheduleText(rawText: String): Result<Int> = withContext(Dispatchers.IO) {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("请输入节假日文本或 JSON 数据"))
        }

        // 1. Try JSON parsing (holiday-cn format)
        if (trimmed.startsWith("{")) {
            val adapter = HolidayNetworkClient.moshi.adapter(HolidayCnResponse::class.java)
            val parsed = runCatching { adapter.fromJson(trimmed) }.getOrNull()
            if (parsed != null && parsed.days.isNotEmpty()) {
                val entities = parsed.days.mapNotNull { day ->
                    val d = SimpleDate.parseIso(day.date) ?: return@mapNotNull null
                    HolidayDayEntity(
                        dateIso = d.toIsoString(),
                        year = d.year,
                        name = if (day.isOffDay) day.name else "${day.name}调休补班",
                        isOffDay = day.isOffDay,
                        overrideType = if (day.isOffDay) ChinaDayType.STATUTORY_HOLIDAY.name else ChinaDayType.LIEU_WORKDAY.name,
                        isUserOverride = true,
                        source = "manual_import"
                    )
                }
                if (entities.isNotEmpty()) {
                    holidayDao.upsertAll(entities)
                    preferencesRepository.recordSyncSuccess(
                        source = "手动 JSON 导入",
                        yearsLabel = entities.map { it.year }.distinct().sorted().joinToString(", ")
                    )
                    return@withContext Result.success(entities.size)
                }
            }
        }

        // 2. Parse line-by-line format: YYYY-MM-DD 休/班 [名称]
        val imported = mutableListOf<HolidayDayEntity>()
        trimmed.lines().forEach { line ->
            val cleaned = line.trim()
            if (cleaned.isEmpty() || cleaned.startsWith("#")) return@forEach
            val tokens = cleaned.split(Regex("[\\s,，]+")).filter { it.isNotBlank() }
            if (tokens.size >= 2) {
                val parsedDate = SimpleDate.parseIso(tokens[0]) ?: return@forEach
                val flagToken = tokens[1]
                val isOff = flagToken.contains("休") || flagToken.contains("假") ||
                    flagToken.equals("off", ignoreCase = true) || flagToken.equals("true", ignoreCase = true)
                val isWork = flagToken.contains("班") || flagToken.contains("补") ||
                    flagToken.equals("work", ignoreCase = true) || flagToken.equals("false", ignoreCase = true)
                if (!isOff && !isWork) return@forEach
                val name = tokens.drop(2).joinToString(" ").ifBlank {
                    if (isOff) "手动节假日" else "手动调休补班"
                }
                imported.add(
                    HolidayDayEntity(
                        dateIso = parsedDate.toIsoString(),
                        year = parsedDate.year,
                        name = name,
                        isOffDay = isOff,
                        overrideType = if (isOff) ChinaDayType.STATUTORY_HOLIDAY.name else ChinaDayType.LIEU_WORKDAY.name,
                        isUserOverride = true,
                        source = "manual_import"
                    )
                )
            }
        }

        if (imported.isEmpty()) {
            Result.failure(IllegalArgumentException("未能识别有效日期，请使用「2027-01-01 休 元旦」或标准 JSON 格式"))
        } else {
            holidayDao.upsertAll(imported)
            preferencesRepository.recordSyncSuccess(
                source = "手动批量导入",
                yearsLabel = imported.map { it.year }.distinct().sorted().joinToString(", ")
            )
            Result.success(imported.size)
        }
    }
}
