package com.codequest.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Route {
    @Serializable data object Splash : Route()
    @Serializable data object Auth : Route()
    @Serializable data object Home : Route()
    @Serializable data class Language(val languageId: String) : Route()
    @Serializable data class Quiz(val lessonId: String) : Route()
    @Serializable data class LessonComplete(val score: Int, val maxScore: Int) : Route()
    @Serializable data object Leaderboard : Route()
    @Serializable data object Profile : Route()
}
