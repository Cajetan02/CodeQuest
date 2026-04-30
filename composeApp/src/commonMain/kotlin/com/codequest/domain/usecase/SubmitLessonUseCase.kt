package com.codequest.domain.usecase

import com.codequest.domain.model.Attempt
import com.codequest.domain.repository.AttemptRepository
import com.codequest.domain.repository.UserStatsRepository

class SubmitLessonUseCase(
    private val attemptRepository: AttemptRepository,
    private val userStatsRepository: UserStatsRepository
) {
    suspend operator fun invoke(attempt: Attempt): Result<Unit> {
        val attemptResult = attemptRepository.recordAttempt(attempt)
        if (attemptResult.isSuccess && attempt.passed) {
            userStatsRepository.addXp(attempt.userId, attempt.score)
            val statsResult = userStatsRepository.updateStreak(attempt.userId)
            
            // Check achievements
            val attempts = attemptRepository.getAttemptsForUser(attempt.userId).getOrNull() ?: emptyList()
            val completedLessons = attempts.filter { it.passed }.map { it.lessonId }.toSet()
            
            if (completedLessons.size == 1) {
                userStatsRepository.unlockAchievement(attempt.userId, "first_lesson")
            }
            
            val currentStreak = statsResult.getOrNull()?.currentStreak ?: 0
            if (currentStreak >= 3) {
                userStatsRepository.unlockAchievement(attempt.userId, "streak_3")
            }
        }
        return attemptResult
    }
}
