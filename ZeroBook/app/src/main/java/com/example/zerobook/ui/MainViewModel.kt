package com.example.zerobook.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerobook.data.repository.AuthRepository
import com.example.zerobook.data.repository.ShelfRepository
import com.example.zerobook.data.repository.UserPreferencesRepository
import com.example.zerobook.model.AppUser
import com.example.zerobook.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthState {
    data object Loading : AuthState
    data object SignedOut : AuthState
    data class SignedIn(val user: AppUser) : AuthState
}

data class MainUiState(
    val authState: AuthState = AuthState.Loading,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

@HiltViewModel
class MainViewModel @Inject constructor(
    authRepository: AuthRepository,
    preferencesRepository: UserPreferencesRepository,
    private val shelfRepository: ShelfRepository,
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = combine(
        authRepository.currentUser,
        preferencesRepository.preferences,
    ) { user, prefs ->
        MainUiState(
            authState = if (user == null) AuthState.SignedOut else AuthState.SignedIn(user),
            themeMode = prefs.themeMode,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MainUiState())

    init {
        // Mỗi khi có user đăng nhập (hoặc mở app khi đã đăng nhập) → kéo kệ sách từ Firestore về
        viewModelScope.launch {
            authRepository.currentUser
                .map { it?.uid }
                .distinctUntilChanged()
                .filterNotNull()
                .collect { shelfRepository.requestSync() }
        }
    }
}
