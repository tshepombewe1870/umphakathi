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

import com.sumsokol.umphakathi.domain.model.Notice
import com.sumsokol.umphakathi.domain.model.NoticeType
import com.sumsokol.umphakathi.domain.model.NoticeStatus

data class CommunityDetailUiState(
    val community: Community? = null,
    val posts: List<CommunityPost> = emptyList(),
    val notices: List<Notice> = emptyList(),
    val isMember: Boolean = false,
    val isLoading: Boolean = true
)

class CommunityDetailViewModel(private val communityId: String) : ViewModel() {
    private val communityRepository = FirebaseDataModule.communityRepository
    private val reportRepository = FirebaseDataModule.reportRepository

    private val _uiState = MutableStateFlow(CommunityDetailUiState())
    val uiState: StateFlow<CommunityDetailUiState> = _uiState.asStateFlow()

    fun joinCommunity() {
        val userId = FirebaseDataModule.currentUserId ?: return
        viewModelScope.launch {
            communityRepository.joinCommunity(communityId, userId)
        }
    }

    fun leaveCommunity() {
        val userId = FirebaseDataModule.currentUserId ?: return
        viewModelScope.launch {
            communityRepository.leaveCommunity(communityId, userId)
        }
    }

    fun shareReport(reportId: String) {
        val userId = FirebaseDataModule.currentUserId ?: return
        viewModelScope.launch {
            reportRepository.shareReport(reportId, userId)
        }
    }

    fun createNotice(title: String, body: String, type: NoticeType, startTime: java.time.Instant?, endTime: java.time.Instant?, location: String?) {
        val userId = FirebaseDataModule.currentUserId ?: return
        val community = _uiState.value.community ?: return
        
        // Check if user is an admin or moderator to bypass approval
        val isPrivileged = userId == community.ownerId || community.moderatorIds.contains(userId)
        val initialStatus = if (isPrivileged || type == NoticeType.PLANNED_OUTAGE) NoticeStatus.APPROVED else NoticeStatus.PENDING_APPROVAL
        
        viewModelScope.launch {
            val user = FirebaseDataModule.userRepository.getOrCreateUser(userId)
            val notice = Notice(
                id = "",
                communityId = communityId,
                creatorId = userId,
                creatorName = user.username,
                creatorType = if (user.accountType.name == "ORGANIZATION") "ORGANIZATION" else "PERSON",
                title = title,
                body = body,
                type = type,
                status = initialStatus,
                startTime = startTime,
                endTime = endTime,
                locationDescription = location
            )
            communityRepository.createNotice(notice)
        }
    }

    fun approveNotice(noticeId: String) {
        viewModelScope.launch {
            communityRepository.updateNoticeStatus(noticeId, NoticeStatus.APPROVED)
        }
    }

    fun rejectNotice(noticeId: String) {
        viewModelScope.launch {
            communityRepository.updateNoticeStatus(noticeId, NoticeStatus.REJECTED)
        }
    }

    init {
        val userId = FirebaseDataModule.currentUserId ?: ""
        viewModelScope.launch {
            combine(
                communityRepository.getCommunity(communityId),
                communityRepository.getPostsForCommunity(communityId),
                communityRepository.getNoticesForCommunity(communityId),
                communityRepository.isUserMember(communityId, userId)
            ) { community, posts, notices, isMember ->
                CommunityDetailUiState(
                    community = community,
                    posts = posts.sortedByDescending { it.createdAt },
                    notices = notices.sortedByDescending { it.createdAt },
                    isMember = isMember,
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
