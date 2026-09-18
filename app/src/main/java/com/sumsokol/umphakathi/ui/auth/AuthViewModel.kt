package com.sumsokol.umphakathi.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sumsokol.umphakathi.data.local.SessionManager
import com.sumsokol.umphakathi.data.firebase.FirebaseDataModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Persona(
    val id: String,
    val name: String,
    val role: String,
    val community: String
)

data class AuthUiState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager.getInstance(application)
    private val userRepository = FirebaseDataModule.userRepository
    
    private val _uiState = MutableStateFlow(AuthUiState(isAuthenticated = sessionManager.getUserId() != null))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val personas = listOf(
        Persona("user-thabo", "Thabo Nkosi", "Resident", "Soweto West"),
        Persona("user-lerato", "Lerato Mokoena", "Moderator", "Soweto West"),
        Persona("user-water-dept", "Joburg Water Official", "Official", "Department of Water"),
        Persona("user-naledi", "Naledi Khumalo", "Resident", "Alexandra Township"),
        Persona("user-saps", "SAPS Alexandra Duty Officer", "Responder", "SAPS")
    )

    fun loginAs(persona: Persona) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Ensure user exists in Firestore with persona details
                userRepository.getOrCreateUser(
                    uid = persona.id,
                    username = persona.name,
                    role = persona.role,
                    communityName = persona.community
                )
                
                sessionManager.saveUserId(persona.id)
                _uiState.value = _uiState.value.copy(isLoading = false, isAuthenticated = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
        _uiState.value = _uiState.value.copy(isAuthenticated = false)
    }

    fun getCurrentUserId(): String? = sessionManager.getUserId()
}
