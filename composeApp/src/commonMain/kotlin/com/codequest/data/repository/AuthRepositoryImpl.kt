package com.codequest.data.repository

import com.codequest.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    override val currentUserId: Flow<String?> = supabaseClient.auth.sessionStatus.map {
        when(it) {
            is SessionStatus.Authenticated -> it.session.user?.id
            else -> null
        }
    }

    override val currentUserName: Flow<String?> = supabaseClient.auth.sessionStatus.map {
        when(it) {
            is SessionStatus.Authenticated -> it.session.user?.userMetadata?.get("full_name")?.let { json ->
                if (json is kotlinx.serialization.json.JsonPrimitive) json.content else null
            }
            else -> null
        }
    }

    override suspend fun loginWithProvider(provider: String, deepLink: String?): Result<Unit> {
        return try {
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInAnonymously(): Result<Unit> {
        return try {
            supabaseClient.auth.signUpWith(Email) {
                email = "anon_${kotlin.random.Random.nextInt()}@example.com"
                password = "Password123!"
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            supabaseClient.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithEmail(emailStr: String, passwordStr: String): Result<Unit> {
        return try {
            supabaseClient.auth.signInWith(Email) {
                email = emailStr
                password = passwordStr
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(emailStr: String, passwordStr: String, name: String): Result<Unit> {
        return try {
            supabaseClient.auth.signUpWith(Email) {
                email = emailStr
                password = passwordStr
                data = kotlinx.serialization.json.buildJsonObject {
                    put("full_name", kotlinx.serialization.json.JsonPrimitive(name))
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
