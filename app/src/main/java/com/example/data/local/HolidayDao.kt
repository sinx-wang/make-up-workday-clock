package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.HolidayDayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HolidayDao {
    @Query("SELECT * FROM holiday_days ORDER BY dateIso ASC")
    fun observeAllHolidays(): Flow<List<HolidayDayEntity>>

    @Query("SELECT * FROM holiday_days WHERE year = :year ORDER BY dateIso ASC")
    fun observeHolidaysForYear(year: Int): Flow<List<HolidayDayEntity>>

    @Query("SELECT * FROM holiday_days ORDER BY dateIso ASC")
    suspend fun getAllHolidaysOnce(): List<HolidayDayEntity>

    @Query("SELECT * FROM holiday_days WHERE dateIso = :dateIso")
    suspend fun getHolidayByDate(dateIso: String): HolidayDayEntity?

    @Query("SELECT dateIso FROM holiday_days WHERE isUserOverride = 1")
    suspend fun getUserOverrideDates(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(days: List<HolidayDayEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(day: HolidayDayEntity)

    @Query("DELETE FROM holiday_days WHERE year = :year AND isUserOverride = 0")
    suspend fun deleteNonOverridesForYear(year: Int)

    @Query("DELETE FROM holiday_days WHERE dateIso = :dateIso")
    suspend fun deleteByDate(dateIso: String)

    @Query("DELETE FROM holiday_days WHERE isUserOverride = 1")
    suspend fun clearAllUserOverrides()

    @Query("SELECT COUNT(*) FROM holiday_days")
    suspend fun countHolidays(): Int
}
