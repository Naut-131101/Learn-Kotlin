package com.example.zerobook.ui

import com.example.zerobook.MainDispatcherRule
import com.example.zerobook.R
import com.example.zerobook.fakes.FakeAuthRepository
import com.example.zerobook.ui.login.LoginViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = FakeAuthRepository()
    private val viewModel = LoginViewModel(authRepository)

    @Test
    fun signInStarted_showsLoading() {
        viewModel.onSignInStarted()
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun idTokenReceived_success_signsInWithoutError() {
        viewModel.onSignInStarted()
        viewModel.onIdTokenReceived("token-123")

        assertEquals("token-123", authRepository.receivedIdToken)
        assertEquals("uid-1", authRepository.user.value?.uid)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessageRes)
    }

    @Test
    fun idTokenReceived_firebaseFailure_showsError() {
        authRepository.signInResult = Result.failure(IllegalStateException("boom"))

        viewModel.onSignInStarted()
        viewModel.onIdTokenReceived("token-123")

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(R.string.login_error_firebase, viewModel.uiState.value.errorMessageRes)
    }

    @Test
    fun cancelled_returnsToIdleWithoutError() {
        viewModel.onSignInStarted()
        viewModel.onSignInCancelled()

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessageRes)
    }
}
