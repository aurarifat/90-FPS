package com.example.data.repository

import com.example.data.local.dao.ActivityLogDao
import com.example.data.local.entity.ActivityLogItem
import kotlinx.coroutines.flow.Flow

class ActivityLogRepository(private val dao: ActivityLogDao) {
    val recentLogs: Flow<List<ActivityLogItem>> = dao.getRecentLogs()

    suspend fun log(
        targetName: String,
        actionType: String,
        details: String,
        packageName: String? = null,
        previousValue: String? = null,
        newValue: String? = null,
        success: Boolean = true,
        isUndoable: Boolean = false
    ): Long {
        val item = ActivityLogItem(
            packageName = packageName,
            targetName = targetName,
            actionType = actionType,
            details = details,
            previousValue = previousValue,
            newValue = newValue,
            success = success,
            isUndoable = isUndoable
        )
        return dao.insertLog(item)
    }

    suspend fun getLatestUndoableLog(): ActivityLogItem? {
        return dao.getLatestUndoableLog()
    }

    suspend fun markUndone(id: Long) {
        dao.markUndone(id)
    }

    suspend fun clearLogs() {
        dao.clearAll()
    }
}
