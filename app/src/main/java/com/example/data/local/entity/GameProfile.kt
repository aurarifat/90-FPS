package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a saved configuration profile for an individual game or app.
 */
@Entity(tableName = "game_profiles")
data class GameProfile(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val targetFps: Int = 90, // 30, 60, 90, 120, or 0 (Auto)
    val requestedRefreshRate: Float = 90f, // 60f, 90f, 120f, or 0f (Default)
    val lockMinAndMaxRate: Boolean = true, // Locks min=90 and peak=90 to eliminate dynamic refresh rate switching jitter
    val stabilityProfile: String = "STABILIZED_90", // "HARD_90_LOCK", "STABILIZED_90", "ROCK_SOLID_60"
    val requestGameMode: Boolean = true, // Requests Android Performance Game Mode where supported
    val autoActivate: Boolean = true,
    val aggressiveMemoryTrim: Boolean = false,
    val customNotes: String = "",
    val isGame: Boolean = true,
    val lastAppliedTimestamp: Long = 0L
)
