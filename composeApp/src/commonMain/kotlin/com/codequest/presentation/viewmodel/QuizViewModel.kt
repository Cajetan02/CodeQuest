package com.codequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codequest.domain.model.Question
import com.codequest.domain.repository.LanguageRepository
import com.codequest.domain.usecase.SubmitLessonUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import com.codequest.domain.model.Attempt
import com.codequest.domain.model.Lesson
import com.codequest.domain.repository.AuthRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock

class QuizViewModel(
    private val languageRepository: LanguageRepository,
    private val submitLessonUseCase: SubmitLessonUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private var questionPool: List<Question> = emptyList()

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions

    private val _lesson = MutableStateFlow<Lesson?>(null)
    val lesson: StateFlow<Lesson?> = _lesson

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex

    fun loadQuestions(lessonId: String) {
        viewModelScope.launch {
            val lessonResult = languageRepository.getLessonById(lessonId)
            if (lessonResult.isSuccess) {
                _lesson.value = lessonResult.getOrThrow()
            }
            
            val result = languageRepository.getQuestionsForLesson(lessonId)
            if (result.isSuccess) {
                val allQuestions = result.getOrThrow()
                questionPool = allQuestions
                // Pick a subset of questions dynamically (e.g., max 5)
                _questions.value = allQuestions.shuffled().take(5)
                _currentQuestionIndex.value = 0
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load questions"
            }
        }
    }

    fun onAnswerSubmitted(isCorrect: Boolean) {
        if (!isCorrect) {
            val currentIds = _questions.value.map { it.id }.toSet()
            val available = questionPool.filter { it.id !in currentIds }
            if (available.isNotEmpty()) {
                val nextQ = available.random()
                _questions.value = _questions.value + nextQ
            } else {
                val currentQ = _questions.value[_currentQuestionIndex.value]
                _questions.value = _questions.value + currentQ.copy(id = currentQ.id + "_retry")
            }
        }
    }

    fun nextQuestion() {
        val nextIndex = _currentQuestionIndex.value + 1
        if (nextIndex < _questions.value.size) {
            _currentQuestionIndex.value = nextIndex
        }
    }

    fun submitLesson(lessonId: String, score: Int, maxScore: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            val userId = authRepository.currentUserId.firstOrNull() ?: return@launch
            
            
            val attempt = Attempt(
                id = randomUUID(),
                userId = userId,
                lessonId = lessonId,
                score = score,
                maxScore = maxScore,
                passed = (score.toFloat() / maxScore) >= 0.5f,
                timestamp = Clock.System.now().toEpochMilliseconds()
            )
            val result = submitLessonUseCase(attempt)
            if (result.isSuccess) {
                onComplete()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Unknown error"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun randomUUID(): String {
        val chars = "0123456789abcdef"
        val variantChars = "89ab"
        var uuid = ""
        for (i in 0 until 36) {
            uuid += when (i) {
                8, 13, 18, 23 -> "-"
                14 -> "4"
                19 -> variantChars.random()
                else -> chars.random()
            }
        }
        return uuid
    }
}
