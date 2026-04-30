package com.codequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codequest.domain.model.Language
import com.codequest.domain.model.UserStats
import com.codequest.domain.repository.UserStatsRepository
import com.codequest.domain.usecase.GetLanguagesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: com.codequest.domain.repository.AuthRepository,
    private val userStatsRepository: UserStatsRepository,
    private val getLanguagesUseCase: GetLanguagesUseCase
) : ViewModel() {

    private val _languages = MutableStateFlow<List<Language>>(emptyList())
    val languages: StateFlow<List<Language>> = _languages

    private val _stats = MutableStateFlow<UserStats?>(null)
    val stats: StateFlow<UserStats?> = _stats

    fun loadHomeData() {
        viewModelScope.launch {
            authRepository.currentUserId.collect { uid ->
                if (uid != null) {
                    val statsResult = userStatsRepository.getUserStats(uid)
                    if (statsResult.isSuccess) {
                        _stats.value = statsResult.getOrThrow()
                    }
                }
            }
        }
        viewModelScope.launch {
            getLanguagesUseCase().onSuccess {
                _languages.value = it
            }
        }
    }

    fun refreshStats() {
        viewModelScope.launch {
            val uid = authRepository.currentUserId.firstOrNull() ?: return@launch
            val statsResult = userStatsRepository.getUserStats(uid)
            if (statsResult.isSuccess) {
                _stats.value = statsResult.getOrThrow()
            }
        }
    }
}
