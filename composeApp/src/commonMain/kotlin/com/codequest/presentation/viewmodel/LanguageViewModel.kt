package com.codequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codequest.domain.model.Lesson
import com.codequest.domain.repository.AttemptRepository
import com.codequest.domain.repository.AuthRepository
import com.codequest.domain.repository.LanguageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class LanguageViewModel(
    private val languageRepository: LanguageRepository,
    private val attemptRepository: AttemptRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _lessons = MutableStateFlow<List<Lesson>>(emptyList())
    val lessons: StateFlow<List<Lesson>> = _lessons

    private val _completedLessonIds = MutableStateFlow<Set<String>>(emptySet())
    val completedLessonIds: StateFlow<Set<String>> = _completedLessonIds

    private val _isLanguageCompleted = MutableStateFlow(false)
    val isLanguageCompleted: StateFlow<Boolean> = _isLanguageCompleted

    fun loadLessons(languageId: String) {
        viewModelScope.launch {
            val result = languageRepository.getLessonsForLanguage(languageId)
            if (result.isSuccess) {
                // In a real app we might determine unlock status based on attempts
                _lessons.value = result.getOrThrow()
            }

            val uid = authRepository.currentUserId.firstOrNull()
            if (uid != null) {
                val attemptsResult = attemptRepository.getAttemptsForUser(uid)
                if (attemptsResult.isSuccess) {
                    val attempts = attemptsResult.getOrThrow()
                    val passedIds = attempts.filter { it.passed }.map { it.lessonId }.toSet()
                    _completedLessonIds.value = passedIds
                    _isLanguageCompleted.value = _lessons.value.isNotEmpty() && passedIds.containsAll(_lessons.value.map { it.id })
                }
            }
        }
    }
}
