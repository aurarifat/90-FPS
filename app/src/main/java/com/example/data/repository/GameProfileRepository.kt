package com.example.data.repository

import com.example.data.local.dao.GameProfileDao
import com.example.data.local.entity.GameProfile
import kotlinx.coroutines.flow.Flow

class GameProfileRepository(private val dao: GameProfileDao) {
    val allProfiles: Flow<List<GameProfile>> = dao.getAllProfiles()

    suspend fun getProfile(packageName: String): GameProfile? {
        return dao.getProfile(packageName)
    }

    suspend fun getAutoActivateProfiles(): List<GameProfile> {
        return dao.getAutoActivateProfiles()
    }

    suspend fun saveProfile(profile: GameProfile) {
        dao.insertOrUpdate(profile)
    }

    suspend fun deleteProfile(profile: GameProfile) {
        dao.delete(profile)
    }

    suspend fun deleteByPackage(packageName: String) {
        dao.deleteByPackage(packageName)
    }
}
