package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tracks changes made to device settings, refresh rates, or booster actions.
 */
@Entity(tableName = "activity_logs")
data class ActivityLogItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val packageName: String? = null,
    val targetName: String,
    val actionType: String, // e.g. "REFRESH_RATE_CHANGE", "PROFILE_ACTIVATED", "RESTORED", "THERMAL_THROTTLE"
    val details: String,
    val previousValue: String? = null,
    val newValue: String? = null,
    val success: Boolean = true,
    val isUndoable: Boolean = false
)
