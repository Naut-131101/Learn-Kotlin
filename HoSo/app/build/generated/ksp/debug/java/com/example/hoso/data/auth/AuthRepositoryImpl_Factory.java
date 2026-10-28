package com.example.hoso.data.auth;

import android.content.Context;
import androidx.credentials.CredentialManager;
import com.google.firebase.auth.FirebaseAuth;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class AuthRepositoryImpl_Factory implements Factory<AuthRepositoryImpl> {
  private final Provider<FirebaseAuth> authProvider;

  private final Provider<CredentialManager> credentialManagerProvider;

  private final Provider<Context> appContextProvider;

  private AuthRepositoryImpl_Factory(Provider<FirebaseAuth> authProvider,
      Provider<CredentialManager> credentialManagerProvider, Provider<Context> appContextProvider) {
    this.authProvider = authProvider;
    this.credentialManagerProvider = credentialManagerProvider;
    this.appContextProvider = appContextProvider;
  }

  @Override
  public AuthRepositoryImpl get() {
    return newInstance(authProvider.get(), credentialManagerProvider.get(), appContextProvider.get());
  }

  public static AuthRepositoryImpl_Factory create(Provider<FirebaseAuth> authProvider,
      Provider<CredentialManager> credentialManagerProvider, Provider<Context> appContextProvider) {
    return new AuthRepositoryImpl_Factory(authProvider, credentialManagerProvider, appContextProvider);
  }

  public static AuthRepositoryImpl newInstance(FirebaseAuth auth,
      CredentialManager credentialManager, Context appContext) {
    return new AuthRepositoryImpl(auth, credentialManager, appContext);
  }
}
