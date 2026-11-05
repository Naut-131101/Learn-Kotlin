package com.example.zerobook.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.zerobook.model.ThemeMode
import com.example.zerobook.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

/** Lưu cài đặt nhỏ (theme, mục tiêu năm, giờ nhắc) bằng DataStore Preferences. */
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val YEARLY_GOAL = intPreferencesKey("yearly_goal")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    }

    override val preferences: Flow<UserPreferences> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            val defaults = UserPreferences()
            UserPreferences(
                themeMode = prefs[Keys.THEME_MODE]
                    ?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
                    ?: defaults.themeMode,
                yearlyGoal = prefs[Keys.YEARLY_GOAL] ?: defaults.yearlyGoal,
                reminderEnabled = prefs[Keys.REMINDER_ENABLED] ?: defaults.reminderEnabled,
                reminderHour = prefs[Keys.REMINDER_HOUR] ?: defaults.reminderHour,
                reminderMinute = prefs[Keys.REMINDER_MINUTE] ?: defaults.reminderMinute,
            )
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    override suspend fun setYearlyGoal(goal: Int) {
        dataStore.edit { it[Keys.YEARLY_GOAL] = goal }
    }

    override suspend fun setReminder(enabled: Boolean, hour: Int, minute: Int) {
        dataStore.edit {
            it[Keys.REMINDER_ENABLED] = enabled
            it[Keys.REMINDER_HOUR] = hour
            it[Keys.REMINDER_MINUTE] = minute
        }
    }
}
