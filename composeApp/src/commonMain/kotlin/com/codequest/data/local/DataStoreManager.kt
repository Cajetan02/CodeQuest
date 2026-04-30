package com.codequest.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreManager(private val dataStore: DataStore<Preferences>) {
    
    companion object {
        val USER_ID_KEY = stringPreferencesKey("user_id")
        val CACHED_LANGUAGES_KEY = stringPreferencesKey("cached_languages")
        val CACHED_STATS_KEY = stringPreferencesKey("cached_stats")
    }

    suspend fun saveUserId(userId: String) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    fun getUserId(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }
    }

    suspend fun clearUserId() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
        }
    }

    suspend fun saveCachedLanguages(json: String) {
        dataStore.edit { preferences ->
            preferences[CACHED_LANGUAGES_KEY] = json
        }
    }

    fun getCachedLanguages(): Flow<String?> = dataStore.data.map { it[CACHED_LANGUAGES_KEY] }

    suspend fun saveCachedStats(json: String) {
        dataStore.edit { preferences ->
            preferences[CACHED_STATS_KEY] = json
        }
    }

    fun getCachedStats(): Flow<String?> = dataStore.data.map { it[CACHED_STATS_KEY] }

    suspend fun saveCachedLessons(languageId: String, json: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("cached_lessons_$languageId")] = json
        }
    }

    fun getCachedLessons(languageId: String): Flow<String?> = dataStore.data.map { 
        it[stringPreferencesKey("cached_lessons_$languageId")] 
    }

    suspend fun saveCachedQuestions(lessonId: String, json: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("cached_questions_$lessonId")] = json
        }
    }

    fun getCachedQuestions(lessonId: String): Flow<String?> = dataStore.data.map { 
        it[stringPreferencesKey("cached_questions_$lessonId")] 
    }
}
