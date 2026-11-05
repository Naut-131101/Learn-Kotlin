package com.example.zerobook.di

import com.example.zerobook.data.repository.AuthRepository
import com.example.zerobook.data.repository.AuthRepositoryImpl
import com.example.zerobook.data.repository.BookRepository
import com.example.zerobook.data.repository.BookRepositoryImpl
import com.example.zerobook.data.repository.ShelfRepository
import com.example.zerobook.data.repository.ShelfRepositoryImpl
import com.example.zerobook.data.repository.UserPreferencesRepository
import com.example.zerobook.data.repository.UserPreferencesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** @Binds: "khi ai cần interface X thì đưa bản cài đặt XImpl". */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBookRepository(impl: BookRepositoryImpl): BookRepository

    @Binds
    @Singleton
    abstract fun bindShelfRepository(impl: ShelfRepositoryImpl): ShelfRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl,
    ): UserPreferencesRepository
}
