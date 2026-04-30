package com.codequest.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Language(
    val id: String,
    val name: String,
    val description: String,
    @SerialName("icon_url")
    val iconUrl: String? = null,
    @SerialName("isUnlocked") // We add this just in case they add it in SQL later
    val isUnlocked: Boolean = false
)

@Serializable
enum class Difficulty {
    @SerialName("easy") BEGINNER,
    @SerialName("medium") INTERMEDIATE,
    @SerialName("hard") ADVANCED
}

@Serializable
data class Lesson(
    val id: String,
    @SerialName("language_id")
    val languageId: String,
    val title: String,
    @SerialName("order_index")
    val orderIndex: Int = 0,
    val content: String? = null,
    val difficulty: String? = null
)

@Serializable
enum class QuestionType {
    @SerialName("multiple_choice") MULTIPLE_CHOICE,
    @SerialName("fill_in_blank") FILL_IN_BLANK,
    @SerialName("code_order") CODE_ORDER,
    @SerialName("true_false") TRUE_FALSE,
    @SerialName("code_snippet") CODE_SNIPPET,
    @SerialName("matching") MATCHING,
    @SerialName("word_bank") WORD_BANK,
    @SerialName("listen_type") LISTEN_TYPE,
    @SerialName("tap_pairs") TAP_PAIRS
}

@Serializable
data class Question(
    val id: String,
    @SerialName("lesson_id")
    val lessonId: String,
    val type: QuestionType,
    val prompt: String,
    val options: List<String> = emptyList(),
    @SerialName("correct_answer_index")
    val correctAnswerIndex: Int? = null,
    @SerialName("correct_answer")
    val correctText: String? = null,
    @SerialName("correct_order")
    val correctOrder: List<String>? = null,
    @SerialName("code_snippet")
    val codeSnippet: String? = null,
    val explanation: String? = null,
    @SerialName("xp_reward")
    val xpReward: Int = 10,
    val difficulty: String? = null
)

@Serializable
data class UserStats(
    val uid: String,
    @SerialName("full_name")
    val fullName: String? = null,
    @SerialName("total_xp")
    val totalXp: Int = 0,
    @SerialName("current_streak")
    val currentStreak: Int = 0,
    @SerialName("longest_streak")
    val longestStreak: Int = 0,
    @SerialName("last_active_date")
    val lastActiveDate: Long? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null
)

@Serializable
data class Attempt(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("lesson_id")
    val lessonId: String,
    val score: Int,
    @SerialName("max_score")
    val maxScore: Int,
    val passed: Boolean,
    val timestamp: Long
)

@Serializable
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    @SerialName("unlock_criteria")
    val unlockCriteria: String,
    @SerialName("icon_url")
    val iconUrl: String? = null
)
