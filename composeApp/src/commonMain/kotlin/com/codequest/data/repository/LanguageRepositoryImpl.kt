package com.codequest.data.repository

import com.codequest.data.local.DataStoreManager
import com.codequest.domain.model.Language
import com.codequest.domain.model.Lesson
import com.codequest.domain.model.Question
import com.codequest.domain.repository.LanguageRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

import com.codequest.domain.model.Difficulty
import com.codequest.domain.model.QuestionType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.firstOrNull

private val json = Json { ignoreUnknownKeys = true }

class LanguageRepositoryImpl(
    private val supabaseClient: SupabaseClient,
    private val dataStoreManager: DataStoreManager
) : LanguageRepository {

    override suspend fun getAvailableLanguages(): Result<List<Language>> {
        return try {
            val languages = supabaseClient.postgrest["languages"].select().decodeList<Language>()
            dataStoreManager.saveCachedLanguages(json.encodeToString(languages))
            Result.success(languages)
        } catch (e: Exception) {
            val cached = dataStoreManager.getCachedLanguages().firstOrNull()
            if (cached != null) {
                try {
                    Result.success(json.decodeFromString(cached))
                } catch (e2: Exception) {
                    Result.failure(e)
                }
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getLessonsForLanguage(languageId: String): Result<List<Lesson>> {
        return try {
            val lessons = supabaseClient.postgrest["lessons"].select {
                filter { eq("language_id", languageId) }
            }.decodeList<Lesson>()
            dataStoreManager.saveCachedLessons(languageId, json.encodeToString(lessons))
            Result.success(lessons)
        } catch (e: Exception) {
            val cached = dataStoreManager.getCachedLessons(languageId).firstOrNull()
            if (cached != null) {
                try {
                    Result.success(json.decodeFromString(cached))
                } catch (e2: Exception) {
                    Result.failure(e)
                }
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getLessonById(lessonId: String): Result<Lesson> {
        return try {
            val lesson = supabaseClient.postgrest["lessons"].select {
                filter { eq("id", lessonId) }
            }.decodeSingle<Lesson>()
            // Not individually caching here to avoid key bloat. It's grouped by language caching.
            Result.success(lesson)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getQuestionsForLesson(lessonId: String): Result<List<Question>> {
        return try {
            val questions = supabaseClient.postgrest["questions"].select {
                filter { eq("lesson_id", lessonId) }
            }.decodeList<Question>()
            dataStoreManager.saveCachedQuestions(lessonId, json.encodeToString(questions))
            Result.success(questions)
        } catch (e: Exception) {
            val cached = dataStoreManager.getCachedQuestions(lessonId).firstOrNull()
            if (cached != null) {
                try {
                    Result.success(json.decodeFromString(cached))
                } catch (e2: Exception) {
                    Result.failure(e)
                }
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun syncLanguages(): Result<Unit> {
        return Result.success(Unit)
    }
}
