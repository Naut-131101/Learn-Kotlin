package com.example.zerobook.data.repository

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.example.zerobook.data.util.suspendRunCatching
import com.example.zerobook.model.AppUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context,
) : AuthRepository {

    /**
     * callbackFlow biến API dạng listener của Firebase thành Flow.
     * Listener được gọi ngay khi đăng ký và mỗi khi đăng nhập/đăng xuất.
     */
    override val currentUser: Flow<AppUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.toAppUser())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): Result<AppUser> =
        suspendRunCatching {
            // Đổi ID token của Google thành phiên đăng nhập Firebase
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val user = auth.signInWithCredential(credential).await().user
                ?: error("Firebase returned no user")
            user.toAppUser()
        }

    override suspend fun signOut() {
        // Xoá trạng thái của Credential Manager để lần sau được chọn lại tài khoản
        suspendRunCatching {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        }.onFailure { Log.w("AuthRepository", "clearCredentialState failed", it) }
        auth.signOut()
    }
}

private fun FirebaseUser.toAppUser() = AppUser(
    uid = uid,
    displayName = displayName,
    email = email,
    photoUrl = photoUrl?.toString(),
)
