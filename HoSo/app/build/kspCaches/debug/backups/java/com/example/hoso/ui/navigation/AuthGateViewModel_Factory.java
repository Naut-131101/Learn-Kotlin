package com.example.hoso.ui.navigation;

import com.example.hoso.data.auth.AuthRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
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
public final class AuthGateViewModel_Factory implements Factory<AuthGateViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private AuthGateViewModel_Factory(Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public AuthGateViewModel get() {
    return newInstance(authRepositoryProvider.get());
  }

  public static AuthGateViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider) {
    return new AuthGateViewModel_Factory(authRepositoryProvider);
  }

  public static AuthGateViewModel newInstance(AuthRepository authRepository) {
    return new AuthGateViewModel(authRepository);
  }
}
