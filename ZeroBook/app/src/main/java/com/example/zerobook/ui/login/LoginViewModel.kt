package com.example.zerobook.ui.login

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerobook.R
import com.example.zerobook.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    @StringRes val errorMessageRes: Int? = null,
)

/**
 * Bước chọn tài khoản Google (Credential Manager) cần Activity nên nằm ở UI.
 * ViewModel nhận ID token và đăng nhập Firebase → phần logic này unit test được.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onSignInStarted() {
        _uiState.value = LoginUiState(isLoading = true)
    }

    fun onIdTokenReceived(idToken: String) {
        viewModelScope.launch {
            val result = authRepository.signInWithGoogleIdToken(idToken)
            // Thành công: MainViewModel thấy user thay đổi và tự chuyển sang app chính
            _uiState.value = if (result.isSuccess) {
                LoginUiState()
            } else {
                LoginUiState(errorMessageRes = R.string.login_error_firebase)
            }
        }
    }

    fun onSignInCancelled() {
        _uiState.value = LoginUiState()
    }

    fun onSignInFailed() {
        _uiState.value = LoginUiState(errorMessageRes = R.string.login_error_google)
    }
}
