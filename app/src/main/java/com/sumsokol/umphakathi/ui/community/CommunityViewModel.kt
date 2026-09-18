package com.sumsokol.umphakathi.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumsokol.umphakathi.data.firebase.FirebaseDataModule
import com.sumsokol.umphakathi.domain.model.Community
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.sumsokol.umphakathi.domain.model.CommunityType

data class CommunityUiState(
    val communities: List<Community> = emptyList(),
    val joinedCommunityIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class CommunityViewModel : ViewModel() {
    private val communityRepository = FirebaseDataModule.communityRepository

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            communityRepository.getCommunities().collect { communities ->
                _uiState.value = _uiState.value.copy(communities = communities, isLoading = false)
            }
        }
    }

    fun joinCommunity(communityId: String) {
        val userId = FirebaseDataModule.currentUserId ?: return
        viewModelScope.launch {
            communityRepository.joinCommunity(communityId, userId)
            _uiState.value = _uiState.value.copy(
                joinedCommunityIds = _uiState.value.joinedCommunityIds + communityId
            )
        }
    }

    fun leaveCommunity(communityId: String) {
        val userId = FirebaseDataModule.currentUserId ?: return
        viewModelScope.launch {
            communityRepository.leaveCommunity(communityId, userId)
            _uiState.value = _uiState.value.copy(
                joinedCommunityIds = _uiState.value.joinedCommunityIds - communityId
            )
        }
    }

    fun createCommunity(name: String, description: String, type: CommunityType, location: String?) {
        val currentUserId = FirebaseDataModule.currentUserId ?: "anonymous"
        viewModelScope.launch {
            val newCommunity = Community(
                id = "",
                name = name,
                description = description,
                type = type,
                creatorId = currentUserId,
                ownerId = currentUserId,
                location = if (location.isNullOrBlank()) null else location,
                memberCount = 1
            )
            communityRepository.createCommunity(newCommunity)
        }
    }
}
