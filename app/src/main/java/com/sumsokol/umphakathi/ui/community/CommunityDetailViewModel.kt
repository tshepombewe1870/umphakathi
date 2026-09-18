package com.sumsokol.umphakathi.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sumsokol.umphakathi.data.firebase.FirebaseDataModule
import com.sumsokol.umphakathi.domain.model.Community
import com.sumsokol.umphakathi.domain.model.CommunityPost
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class CommunityDetailUiState(
    val community: Community? = null,
    val posts: List<CommunityPost> = emptyList(),
    val isLoading: Boolean = true
)

class CommunityDetailViewModel(private val communityId: String) : ViewModel() {
    private val communityRepository = FirebaseDataModule.communityRepository
    private val reportRepository = FirebaseDataModule.reportRepository

    private val _uiState = MutableStateFlow(CommunityDetailUiState())
    val uiState: StateFlow<CommunityDetailUiState> = _uiState.asStateFlow()
    
    // ... init ...

    fun shareReport(reportId: String) {
        val userId = FirebaseDataModule.currentUserId ?: return
        viewModelScope.launch {
            reportRepository.shareReport(reportId, userId)
        }
    }

    init {
        viewModelScope.launch {
            combine(
                communityRepository.getCommunity(communityId),
                communityRepository.getPostsForCommunity(communityId)
            ) { community, posts ->
                CommunityDetailUiState(
                    community = community,
                    posts = posts.sortedByDescending { it.createdAt },
                    isLoading = false
                )
            }.collect { _uiState.value = it }
        }
    }

    companion object {
        fun factory(communityId: String) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CommunityDetailViewModel(communityId) as T
        }
    }
}
