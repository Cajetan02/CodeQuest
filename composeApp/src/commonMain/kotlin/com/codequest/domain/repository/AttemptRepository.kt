package com.codequest.domain.repository

import com.codequest.domain.model.Attempt

interface AttemptRepository {
    suspend fun recordAttempt(attempt: Attempt): Result<Unit>
    suspend fun getAttemptsForUser(userId: String): Result<List<Attempt>>
}
