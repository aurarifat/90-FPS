package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.GameProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface GameProfileDao {
    @Query("SELECT * FROM game_profiles ORDER BY appName ASC")
    fun getAllProfiles(): Flow<List<GameProfile>>

    @Query("SELECT * FROM game_profiles WHERE packageName = :packageName LIMIT 1")
    suspend fun getProfile(packageName: String): GameProfile?

    @Query("SELECT * FROM game_profiles WHERE autoActivate = 1")
    suspend fun getAutoActivateProfiles(): List<GameProfile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: GameProfile)

    @Update
    suspend fun update(profile: GameProfile)

    @Delete
    suspend fun delete(profile: GameProfile)

    @Query("DELETE FROM game_profiles WHERE packageName = :packageName")
    suspend fun deleteByPackage(packageName: String)
}
