package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AlarmGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmGroupDao {
    @Query("SELECT * FROM alarm_groups ORDER BY sortOrder ASC, id ASC")
    fun observeAllGroups(): Flow<List<AlarmGroupEntity>>

    @Query("SELECT * FROM alarm_groups ORDER BY sortOrder ASC, id ASC")
    suspend fun getAllGroupsOnce(): List<AlarmGroupEntity>

    @Query("SELECT * FROM alarm_groups WHERE id = :id")
    suspend fun getGroupById(id: Long): AlarmGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: AlarmGroupEntity): Long

    @Update
    suspend fun updateGroup(group: AlarmGroupEntity)

    @Delete
    suspend fun deleteGroup(group: AlarmGroupEntity)

    @Query("UPDATE alarm_groups SET isEnabled = :enabled, skippedDateIso = NULL WHERE id = :id")
    suspend fun setGroupEnabled(id: Long, enabled: Boolean)

    @Query("UPDATE alarm_groups SET skippedDateIso = :dateIso WHERE id = :id")
    suspend fun setGroupSkippedDate(id: Long, dateIso: String?)

    @Query("SELECT COUNT(*) FROM alarm_groups")
    suspend fun countGroups(): Int
}
