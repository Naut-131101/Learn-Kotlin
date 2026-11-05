package com.example.zerobook.ui.login

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/**
 * Hiện hộp thoại "Đăng nhập bằng Google" của hệ thống (Credential Manager)
 * và trả về ID token để đổi lấy phiên đăng nhập Firebase.
 *
 * [context] phải là Activity context (lấy từ LocalContext trong Compose).
 */
object GoogleSignInHelper {

    suspend fun requestIdToken(context: Context, webClientId: String): String {
        val option = GetSignInWithGoogleOption.Builder(webClientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        val credential = CredentialManager.create(context)
            .getCredential(context, request)
            .credential

        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return GoogleIdTokenCredential.createFrom(credential.data).idToken
        }
        throw IllegalStateException("Unexpected credential type: ${credential.type}")
    }
}
