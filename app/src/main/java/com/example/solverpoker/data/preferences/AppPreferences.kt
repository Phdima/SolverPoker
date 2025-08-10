package com.example.solverpoker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

class AppPreferences(private val dataStore: DataStore<Preferences>) {
    companion object {
        val APP_FIRST_LAUNCH = booleanPreferencesKey("app_first_launch")
        val SCREEN_HINT_SHOWN = booleanPreferencesKey("screen_hint_shown")
    }

    suspend fun setAppFirstLaunch(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[APP_FIRST_LAUNCH] = completed
        }
    }

    suspend fun setScreenHintShown(screenName: String, shown: Boolean) {
        val key = booleanPreferencesKey("hint_$screenName")
        dataStore.edit { preferences ->
            preferences[key] = shown
        }
    }

    val isAppFirstLaunch: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[APP_FIRST_LAUNCH] ?: true
        }

    fun isScreenHintShown(screenName: String): Flow<Boolean> {
        val key = booleanPreferencesKey("hint_$screenName")
        return dataStore.data
            .map { preferences ->
                preferences[key] ?: false
            }
    }
}