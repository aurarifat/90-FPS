package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ActivityLogItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentLogs(): Flow<List<ActivityLogItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLogItem): Long

    @Query("SELECT * FROM activity_logs WHERE id = :id LIMIT 1")
    suspend fun getLogById(id: Long): ActivityLogItem?

    @Query("DELETE FROM activity_logs")
    suspend fun clearAll()

    @Query("SELECT * FROM activity_logs WHERE isUndoable = 1 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestUndoableLog(): ActivityLogItem?

    @Query("UPDATE activity_logs SET isUndoable = 0 WHERE id = :id")
    suspend fun markUndone(id: Long)
}
