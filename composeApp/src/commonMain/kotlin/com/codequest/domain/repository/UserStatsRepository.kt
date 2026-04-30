package com.codequest.domain.repository

import com.codequest.domain.model.Achievement
import com.codequest.domain.model.UserStats
import kotlinx.coroutines.flow.Flow

interface UserStatsRepository {
    val currentUserStats: Flow<UserStats?>
    suspend fun getUserStats(userId: String): Result<UserStats>
    suspend fun addXp(userId: String, amount: Int): Result<UserStats>
    suspend fun updateStreak(userId: String): Result<UserStats>
    suspend fun updateFullName(userId: String, name: String): Result<UserStats>
    suspend fun updateAvatarUrl(userId: String, url: String): Result<UserStats>
    suspend fun getAchievements(userId: String): Result<List<Achievement>>
    suspend fun unlockAchievement(userId: String, achievementId: String): Result<Unit>
}
