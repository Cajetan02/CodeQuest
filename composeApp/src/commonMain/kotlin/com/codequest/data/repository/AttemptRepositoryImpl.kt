package com.codequest.data.repository

import com.codequest.domain.model.Attempt
import com.codequest.domain.repository.AttemptRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class AttemptRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : AttemptRepository {

    override suspend fun recordAttempt(attempt: Attempt): Result<Unit> {
        return try {
            supabaseClient.postgrest["attempts"].insert(attempt)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAttemptsForUser(userId: String): Result<List<Attempt>> {
        return try {
            val attempts = supabaseClient.postgrest["attempts"].select {
                filter { eq("user_id", userId) }
            }.decodeList<Attempt>()
            Result.success(attempts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
