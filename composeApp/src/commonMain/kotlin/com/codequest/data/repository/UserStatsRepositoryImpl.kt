package com.codequest.data.repository

import com.codequest.data.local.DataStoreManager
import com.codequest.domain.model.Achievement
import com.codequest.domain.model.UserStats
import com.codequest.domain.repository.UserStatsRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.firstOrNull

class UserStatsRepositoryImpl(
    private val supabaseClient: SupabaseClient,
    private val dataStoreManager: DataStoreManager
) : UserStatsRepository {

    override val currentUserStats: Flow<UserStats?> = flow {
        // This could be combined with local datastore flows or realtime changes
        emit(null)
    }

    override suspend fun getUserStats(userId: String): Result<UserStats> {
        return try {
            val stats = supabaseClient.postgrest["user_stats"].select {
                filter { eq("uid", userId) }
            }.decodeSingle<UserStats>()
            dataStoreManager.saveCachedStats(Json.encodeToString(stats))
            Result.success(stats)
        } catch (e: Exception) {
            val cached = dataStoreManager.getCachedStats().firstOrNull()
            if (cached != null) {
                Result.success(Json.decodeFromString(cached))
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun addXp(userId: String, amount: Int): Result<UserStats> {
        return try {
            val currentStats = getUserStats(userId).getOrNull()
            
            if (currentStats == null) {
                val newStats = UserStats(uid = userId, totalXp = amount, currentStreak = 0, longestStreak = 0)
                supabaseClient.postgrest["user_stats"].insert(newStats)
                return Result.success(newStats)
            } else {
                val newXp = currentStats.totalXp + amount
                supabaseClient.postgrest["user_stats"].update(
                    {
                        set("total_xp", newXp)
                    }
                ) {
                    filter { eq("uid", userId) }
                }
            }
            // Re-fetch to get the updated record
            getUserStats(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateStreak(userId: String): Result<UserStats> {
        return try {
            val currentMillis = Clock.System.now().toEpochMilliseconds()
            val currentDay = (currentMillis / (1000 * 60 * 60 * 24)).toLong()

            val currentStats = getUserStats(userId).getOrNull()
            if (currentStats == null) {
                val newStats = UserStats(uid = userId, totalXp = 0, currentStreak = 1, longestStreak = 1, lastActiveDate = currentDay)
                supabaseClient.postgrest["user_stats"].insert(newStats)
            } else {
                val lastActiveDay = currentStats.lastActiveDate ?: 0L
                val daysDiff = currentDay - lastActiveDay

                if (daysDiff == 1L) {
                    val newStreak = currentStats.currentStreak + 1
                    val newLongest = maxOf(currentStats.longestStreak, newStreak)
                    supabaseClient.postgrest["user_stats"].update({
                        set("current_streak", newStreak)
                        set("longest_streak", newLongest)
                        set("last_active_date", currentDay)
                    }) { filter { eq("uid", userId) } }
                } else if (daysDiff > 1L) {
                    supabaseClient.postgrest["user_stats"].update({
                        set("current_streak", 1)
                        set("last_active_date", currentDay)
                    }) { filter { eq("uid", userId) } }
                }
                // If daysDiff == 0, already active today, do nothing.
            }

            // Re-fetch to get the updated record
            getUserStats(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateFullName(userId: String, name: String): Result<UserStats> {
        return try {
            supabaseClient.postgrest["user_stats"].update(
                {
                    set("full_name", name)
                }
            ) {
                filter { eq("uid", userId) }
            }
            getUserStats(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAvatarUrl(userId: String, url: String): Result<UserStats> {
        return try {
            supabaseClient.postgrest["user_stats"].update(
                {
                    set("avatar_url", url)
                }
            ) {
                filter { eq("uid", userId) }
            }
            getUserStats(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAchievements(userId: String): Result<List<Achievement>> {
         return try {
            val achievements = supabaseClient.postgrest["achievements"].select().decodeList<Achievement>()
            Result.success(achievements)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unlockAchievement(userId: String, achievementId: String): Result<Unit> {
        return try {
            supabaseClient.postgrest["user_achievements"].insert(
                mapOf("user_id" to userId, "achievement_id" to achievementId)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
