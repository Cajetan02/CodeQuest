package com.codequest.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserId: Flow<String?>
    val currentUserName: Flow<String?>
    suspend fun loginWithProvider(provider: String, deepLink: String? = null): Result<Unit>
    suspend fun signInAnonymously(): Result<Unit>
    suspend fun signInWithEmail(email: String, password: String): Result<Unit>
    suspend fun signUpWithEmail(email: String, password: String, name: String): Result<Unit>
    suspend fun logout(): Result<Unit>
}
