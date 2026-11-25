package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AlarmEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC, id ASC")
    fun observeAllAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC, id ASC")
    suspend fun getAllAlarmsOnce(): List<AlarmEntity>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Long): AlarmEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)

    @Query("UPDATE alarms SET isEnabled = :enabled, skippedDateIso = NULL WHERE id = :id")
    suspend fun setAlarmEnabled(id: Long, enabled: Boolean)

    @Query("UPDATE alarms SET skippedDateIso = :dateIso WHERE id = :id")
    suspend fun setSkippedDate(id: Long, dateIso: String?)

    @Query("SELECT COUNT(*) FROM alarms")
    suspend fun countAlarms(): Int
}
