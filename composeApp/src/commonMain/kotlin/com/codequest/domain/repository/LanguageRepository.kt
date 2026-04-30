package com.codequest.domain.repository

import com.codequest.domain.model.Language
import com.codequest.domain.model.Lesson
import com.codequest.domain.model.Question
import kotlinx.coroutines.flow.Flow

interface LanguageRepository {
    suspend fun getAvailableLanguages(): Result<List<Language>>
    suspend fun getLessonsForLanguage(languageId: String): Result<List<Lesson>>
    suspend fun getQuestionsForLesson(lessonId: String): Result<List<Question>>
    suspend fun getLessonById(lessonId: String): Result<Lesson>
    suspend fun syncLanguages(): Result<Unit>
}
