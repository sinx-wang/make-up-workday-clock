package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AlarmOff
import androidx.compose.material.icons.rounded.AlarmOn
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlarmEntity
import com.example.data.model.AlarmGroupEntity
import com.example.data.model.ChinaDayType
import com.example.data.model.HolidayDayEntity
import com.example.data.model.ResolvedDayInfo
import com.example.data.model.SimpleDate
import com.example.domain.AlarmScheduleCalculator
import com.example.ui.components.DayTypePill
import com.example.ui.components.OneUiCard
import com.example.ui.components.OneUiSectionHeader
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.LocalOneUiColors
import com.example.ui.theme.OutfitDisplayFamily
import com.example.ui.viewmodel.HolidayAlarmUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class DayAlarmEntry(
    val alarm: AlarmEntity,
    val group: AlarmGroupEntity?,
    val isActive: Boolean,
    val inactiveReason: String?
)

@Composable
fun CalendarScreen(
    uiState: HolidayAlarmUiState,
    onSyncYears: (List<Int>) -> Unit,
    onEditDayOverride: (ResolvedDayInfo) -> Unit,
    onOpenManualImport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { SimpleDate.today() }
    var displayYear by remember { mutableIntStateOf(today.year) }
    var displayMonth by remember { mutableIntStateOf(today.month) }
    var selectedDate by remember { mutableStateOf(today) }

    val haptics = LocalHapticFeedback.current
    val oneUi = LocalOneUiColors.current
    val s = LocalAppStrings.current

    val selectedDayInfo = remember(selectedDate, uiState.holidayMap) {
        AlarmScheduleCalculator.resolveDay(selectedDate, uiState.holidayMap)
    }

    val allAlarmsWithGroup = remember(uiState.groups, uiState.ungroupedAlarms) {
        val grouped = uiState.groups.flatMap { grp -> grp.alarms.map { it.alarm to grp.group } }
        val ungrouped = uiState.ungroupedAlarms.map { it.alarm to null }
        (grouped + ungrouped).sortedWith(compareBy({ it.first.hour }, { it.first.minute }, { it.first.id }))
    }

    val selectedDayAlarms = remember(selectedDate, allAlarmsWithGroup, uiState.holidayMap, s.language) {
        allAlarmsWithGroup.map { (alarm, group) ->
            val rings = AlarmScheduleCalculator.shouldRingOnDate(
                alarm = alarm,
                group = group,
                date = selectedDate,
                holidayMap = uiState.holidayMap
            )
            val reason = if (rings) null else s.skipReasonForDate(
                alarm = alarm,
                group = group,
                date = selectedDate,
                holidayMap = uiState.holidayMap
            )
            DayAlarmEntry(
                alarm = alarm,
                group = group,
                isActive = rings,
                inactiveReason = reason
            )
        }
    }

    val activeAlarms = remember(selectedDayAlarms) { selectedDayAlarms.filter { it.isActive } }
    val inactiveAlarms = remember(selectedDayAlarms) { selectedDayAlarms.filter { !it.isActive } }

    val yearHolidays = remember(uiState.holidayMap, displayYear) {
        uiState.holidayMap.values
            .filter { it.year == displayYear }
            .sortedBy { it.dateIso }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Sync & Manual Import Card
        item(key = "sync_control_card") {
            OneUiCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = s.holidaySyncTitle,
                            style = MaterialTheme.typography.titleMedium
                        )
                        val syncMeta = if (uiState.preferences.lastSyncMillis > 0L) {
                            val fmt = SimpleDateFormat("MM-dd HH:mm", Locale.US)
                            s.updatedAt(fmt.format(Date(uiState.preferences.lastSyncMillis)))
                        } else {
                            s.builtinBaseline
                        }
                        Text(
                            text = syncMeta,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onSyncYears(listOf(displayYear, displayYear + 1)) },
                            enabled = !uiState.isSyncing,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("calendar_sync_now_button"),
                            shape = RoundedCornerShape(22.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            if (uiState.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.CloudSync,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (uiState.isSyncing) s.syncing else s.onlineUpdate)
                        }

                        OutlinedButton(
                            onClick = onOpenManualImport,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("calendar_manual_import_button"),
                            shape = RoundedCornerShape(22.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.UploadFile,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(s.manualImport)
                        }
                    }
                }
            }
        }

        // Interactive Monthly Calendar Card
        item(key = "monthly_calendar_card") {
            OneUiCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(2025, 2026, 2027).forEach { y ->
                                FilterChip(
                                    selected = displayYear == y,
                                    onClick = { displayYear = y },
                                    label = { Text(s.yearChip(y)) }
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                displayYear = today.year
                                displayMonth = today.month
                                selectedDate = today
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Today,
                                contentDescription = s.jumpToToday,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (displayMonth == 1) {
                                    displayYear -= 1
                                    displayMonth = 12
                                } else {
                                    displayMonth -= 1
                                }
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = s.prevMonth)
                        }

                        Text(
                            text = s.formatYearMonth(displayYear, displayMonth),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (displayMonth == 12) {
                                    displayYear += 1
                                    displayMonth = 1
                                } else {
                                    displayMonth += 1
                                }
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = s.nextMonth)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        s.weekdayInitials.forEachIndexed { idx, label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (idx >= 5) oneUi.holidayAccent
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    MonthGrid(
                        year = displayYear,
                        month = displayMonth,
                        today = today,
                        selectedDate = selectedDate,
                        holidayMap = uiState.holidayMap,
                        onDayClick = { date ->
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedDate = date
                        }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem(s.calendarBadgeHoliday, s.legendHoliday, oneUi.holidayAccent)
                        LegendItem(s.calendarBadgeLieu, s.legendLieu, oneUi.lieuAccent)
                    }
                }
            }
        }

        // Selected Day Alarm Status Card
        item(key = "selected_day_alarms_card") {
            SelectedDayAlarmsCard(
                dayInfo = selectedDayInfo,
                activeAlarms = activeAlarms,
                inactiveAlarms = inactiveAlarms,
                onEditDayOverride = { onEditDayOverride(selectedDayInfo) }
            )
        }

        // Year Holiday List
        item(key = "year_schedule_header") {
            OneUiSectionHeader(title = s.yearScheduleHeader(displayYear))
        }

        if (yearHolidays.isEmpty()) {
            item(key = "empty_year_holidays") {
                OneUiCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EditCalendar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(s.noRecords, style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        } else {
            items(yearHolidays, key = { it.dateIso }) { entry ->
                val date = SimpleDate.parseIso(entry.dateIso)
                if (date != null) {
                    val resolved = AlarmScheduleCalculator.resolveDay(date, uiState.holidayMap)
                    val isSelected = date == selectedDate
                    OneUiCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp,
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        } else {
                            oneUi.cardSurface
                        },
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            displayMonth = date.month
                            selectedDate = date
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(
                                            if (resolved.dayType == ChinaDayType.STATUTORY_HOLIDAY)
                                                oneUi.holidayContainer
                                            else oneUi.lieuContainer
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = s.dayTypeShortBadge(resolved.dayType),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (resolved.dayType == ChinaDayType.STATUTORY_HOLIDAY)
                                            oneUi.holidayAccent
                                        else oneUi.lieuAccent
                                    )
                                }
                                Text(
                                    text = "${s.formatShortDate(date)} ${s.formatWeekdayShort(date)} · ${s.localizeHolidayLabel(resolved.label, resolved.dayType)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (resolved.isUserOverride) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = s.manualTag,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedDayAlarmsCard(
    dayInfo: ResolvedDayInfo,
    activeAlarms: List<DayAlarmEntry>,
    inactiveAlarms: List<DayAlarmEntry>,
    onEditDayOverride: () -> Unit
) {
    val oneUi = LocalOneUiColors.current
    val s = LocalAppStrings.current

    OneUiCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("selected_day_alarms_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${s.formatShortDate(dayInfo.date)} ${s.formatWeekdayShort(dayInfo.date)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    DayTypePill(
                        dayType = dayInfo.dayType,
                        label = s.localizeHolidayLabel(dayInfo.label, dayInfo.dayType)
                    )
                }

                TextButton(
                    onClick = onEditDayOverride,
                    modifier = Modifier.testTag("edit_selected_day_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(s.adjustDay)
                }
            }

            if (activeAlarms.isEmpty() && inactiveAlarms.isEmpty()) {
                Text(
                    text = s.noAlarmsYet,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.AlarmOn,
                            contentDescription = null,
                            tint = oneUi.activeAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = s.activeAlarmsCount(activeAlarms.size),
                            style = MaterialTheme.typography.labelLarge,
                            color = oneUi.activeAccent
                        )
                    }

                    if (activeAlarms.isEmpty()) {
                        Text(
                            text = s.none,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 22.dp)
                        )
                    } else {
                        activeAlarms.forEach { entry ->
                            DayAlarmRow(
                                entry = entry,
                                accentColor = oneUi.activeAccent,
                                containerColor = oneUi.activeContainer.copy(alpha = 0.55f)
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.AlarmOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = s.inactiveAlarmsCount(inactiveAlarms.size),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (inactiveAlarms.isEmpty()) {
                        Text(
                            text = s.none,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 22.dp)
                        )
                    } else {
                        inactiveAlarms.forEach { entry ->
                            DayAlarmRow(
                                entry = entry,
                                accentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayAlarmRow(
    entry: DayAlarmEntry,
    accentColor: Color,
    containerColor: Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = entry.alarm.formattedTime(),
                    fontFamily = OutfitDisplayFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.alarm.label,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val meta = buildString {
                        entry.group?.let { append(it.name) }
                        if (!entry.isActive && !entry.inactiveReason.isNullOrBlank()) {
                            if (isNotEmpty()) append(" · ")
                            append(entry.inactiveReason)
                        }
                    }
                    if (meta.isNotEmpty()) {
                        Text(
                            text = meta,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthGrid(
    year: Int,
    month: Int,
    today: SimpleDate,
    selectedDate: SimpleDate,
    holidayMap: Map<String, HolidayDayEntity>,
    onDayClick: (SimpleDate) -> Unit
) {
    val firstDay = SimpleDate(year, month, 1)
    val leadingBlanks = firstDay.dayOfWeekIso - 1
    val daysInMonth = SimpleDate.daysInMonth(year, month)
    val totalCells = ((leadingBlanks + daysInMonth + 6) / 7) * 7
    val oneUi = LocalOneUiColors.current
    val s = LocalAppStrings.current

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (row in 0 until (totalCells / 7)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNum = cellIndex - leadingBlanks + 1
                    if (dayNum in 1..daysInMonth) {
                        val date = SimpleDate(year, month, dayNum)
                        val info = AlarmScheduleCalculator.resolveDay(date, holidayMap)
                        val isSelected = date == selectedDate
                        val isToday = date == today

                        val cellBg = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            info.dayType == ChinaDayType.STATUTORY_HOLIDAY -> oneUi.holidayContainer.copy(alpha = 0.72f)
                            info.dayType == ChinaDayType.LIEU_WORKDAY -> oneUi.lieuContainer.copy(alpha = 0.85f)
                            else -> Color.Transparent
                        }

                        val dayNumberColor = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            isToday -> MaterialTheme.colorScheme.primary
                            info.dayType == ChinaDayType.STATUTORY_HOLIDAY -> oneUi.holidayAccent
                            info.dayType == ChinaDayType.LIEU_WORKDAY -> oneUi.lieuAccent
                            info.dayType == ChinaDayType.WEEKEND -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.95f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(cellBg)
                                .then(
                                    when {
                                        !isSelected && isToday -> Modifier.border(
                                            1.5.dp,
                                            MaterialTheme.colorScheme.primary,
                                            RoundedCornerShape(14.dp)
                                        )
                                        !isSelected && info.isUserOverride -> Modifier.border(
                                            1.dp,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                            RoundedCornerShape(14.dp)
                                        )
                                        else -> Modifier
                                    }
                                )
                                .clickable { onDayClick(date) }
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayNum.toString(),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (isSelected || isToday || info.dayType != ChinaDayType.WORKDAY)
                                        FontWeight.Bold else FontWeight.Medium,
                                    color = dayNumberColor
                                )
                                val subBadge = when (info.dayType) {
                                    ChinaDayType.STATUTORY_HOLIDAY -> s.calendarBadgeHoliday
                                    ChinaDayType.LIEU_WORKDAY -> s.calendarBadgeLieu
                                    else -> if (info.isUserOverride) s.calendarBadgeOverride else if (isToday) s.calendarBadgeToday else ""
                                }
                                if (subBadge.isNotEmpty()) {
                                    Text(
                                        text = subBadge,
                                        fontSize = 9.sp,
                                        lineHeight = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else if (info.dayType == ChinaDayType.STATUTORY_HOLIDAY) oneUi.holidayAccent
                                        else if (info.dayType == ChinaDayType.LIEU_WORKDAY) oneUi.lieuAccent
                                        else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.95f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(badge: String, label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(color.copy(alpha = 0.18f))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = badge,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
