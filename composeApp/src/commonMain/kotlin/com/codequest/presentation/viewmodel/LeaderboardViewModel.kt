package com.codequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codequest.domain.model.UserStats
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LeaderboardViewModel(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _leaders = MutableStateFlow<List<UserStats>>(emptyList())
    val leaders: StateFlow<List<UserStats>> = _leaders

    fun fetchLeaderboard() {
        viewModelScope.launch {
            try {
                // Fetch top 50 users by totalXp
                val topUsers = supabaseClient.postgrest["user_stats"]
                    .select {
                        order("total_xp", order = Order.DESCENDING)
                        limit(50)
                    }.decodeList<UserStats>()
                _leaders.value = topUsers
            } catch (e: Exception) {
                // handle error
            }
        }
    }
}
