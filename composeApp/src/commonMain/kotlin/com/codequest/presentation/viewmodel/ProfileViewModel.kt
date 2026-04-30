package com.codequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codequest.domain.model.Achievement
import com.codequest.domain.model.UserStats
import com.codequest.domain.repository.AuthRepository
import com.codequest.domain.repository.UserStatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userStatsRepository: UserStatsRepository
) : ViewModel() {

    private val _stats = MutableStateFlow<UserStats?>(null)
    val stats: StateFlow<UserStats?> = _stats

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements

    fun loadProfile() {
        viewModelScope.launch {
            authRepository.currentUserId.collect { uid ->
                if (uid != null) {
                    val userName = authRepository.currentUserName.firstOrNull()
                    val statsResult = userStatsRepository.getUserStats(uid)
                    if (statsResult.isSuccess) {
                        var currentStats = statsResult.getOrThrow()
                        if (userName != null && currentStats.fullName != userName) {
                            val updateResult = userStatsRepository.updateFullName(uid, userName)
                            if (updateResult.isSuccess) {
                                currentStats = updateResult.getOrThrow()
                            }
                        }
                        _stats.value = currentStats
                    }
                    val achievementsResult = userStatsRepository.getAchievements(uid)
                    if (achievementsResult.isSuccess) {
                        _achievements.value = achievementsResult.getOrThrow()
                    }
                }
            }
        }
    }

    fun updateName(name: String) {
        viewModelScope.launch {
            authRepository.currentUserId.firstOrNull()?.let { uid ->
                val result = userStatsRepository.updateFullName(uid, name)
                if (result.isSuccess) {
                    _stats.value = result.getOrThrow()
                }
            }
        }
    }

    fun updateAvatar(url: String) {
        viewModelScope.launch {
            authRepository.currentUserId.firstOrNull()?.let { uid ->
                val result = userStatsRepository.updateAvatarUrl(uid, url)
                if (result.isSuccess) {
                    _stats.value = result.getOrThrow()
                }
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onSuccess()
        }
    }
}
