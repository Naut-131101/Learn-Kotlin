package com.example.zerobook.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerobook.data.repository.AuthRepository
import com.example.zerobook.data.repository.ShelfRepository
import com.example.zerobook.data.repository.UserPreferencesRepository
import com.example.zerobook.model.AppUser
import com.example.zerobook.model.ThemeMode
import com.example.zerobook.model.UserPreferences
import com.example.zerobook.work.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val user: AppUser? = null,
    val preferences: UserPreferences = UserPreferences(),
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val shelfRepository: ShelfRepository,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        authRepository.currentUser,
        preferencesRepository.preferences,
    ) { user, prefs ->
        SettingsUiState(user = user, preferences = prefs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
    }

    fun setReminderEnabled(enabled: Boolean) {
        val prefs = uiState.value.preferences
        viewModelScope.launch {
            preferencesRepository.setReminder(enabled, prefs.reminderHour, prefs.reminderMinute)
            if (enabled) {
                reminderScheduler.schedule(prefs.reminderHour, prefs.reminderMinute)
            } else {
                reminderScheduler.cancel()
            }
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        val prefs = uiState.value.preferences
        viewModelScope.launch {
            preferencesRepository.setReminder(prefs.reminderEnabled, hour, minute)
            if (prefs.reminderEnabled) reminderScheduler.schedule(hour, minute)
        }
    }

    fun syncNow() {
        shelfRepository.requestSync()
    }

    fun signOut() {
        val prefs = uiState.value.preferences
        viewModelScope.launch {
            reminderScheduler.cancel()
            preferencesRepository.setReminder(false, prefs.reminderHour, prefs.reminderMinute)
            shelfRepository.clearLocal()
            // Gọi cuối cùng: đăng xuất xong, app tự chuyển về màn Login
            authRepository.signOut()
        }
    }
}
