package com.sumsokol.umphakathi.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumsokol.umphakathi.data.firebase.FirebaseDataModule
import com.sumsokol.umphakathi.domain.model.User
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class ProfileViewModel : ViewModel() {
    private val userRepository = FirebaseDataModule.userRepository

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            FirebaseDataModule.userIdFlow.collect { uid ->
                if (uid != null) {
                    loadUserProfile(uid)
                } else {
                    _uiState.value = ProfileUiState(user = null, isLoading = false)
                }
            }
        }
    }

    private fun loadUserProfile(uid: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            userRepository.getUser(uid).collect { user ->
                _uiState.value = ProfileUiState(user = user, isLoading = false)
            }
        }
    }
}
