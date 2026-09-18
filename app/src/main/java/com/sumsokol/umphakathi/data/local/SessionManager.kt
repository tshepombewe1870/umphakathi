package com.sumsokol.umphakathi.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    
    private val _userIdFlow = MutableStateFlow(prefs.getString("current_user_id", null))
    val userIdFlow: StateFlow<String?> = _userIdFlow.asStateFlow()

    fun saveUserId(userId: String) {
        prefs.edit().putString("current_user_id", userId).apply()
        _userIdFlow.value = userId
    }

    fun getUserId(): String? {
        return _userIdFlow.value
    }

    fun clearSession() {
        prefs.edit().remove("current_user_id").apply()
        _userIdFlow.value = null
    }

    companion object {
        @Volatile
        private var instance: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
