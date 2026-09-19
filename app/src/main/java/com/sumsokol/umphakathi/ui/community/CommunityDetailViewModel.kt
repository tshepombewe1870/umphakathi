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
import com.sumsokol.umphakathi.domain.model.PostStatus
import com.sumsokol.umphakathi.domain.model.ReportStatus

data class CommunityDetailUiState(
    val community: Community? = null,
    val posts: List<CommunityPost> = emptyList(),
    val notices: List<Notice> = emptyList(),
    val selectedFilter: String = "All",
    val allCount: Int = 0,
    val resolvedCount: Int = 0,
    val activeCount: Int = 0,
    val isMember: Boolean = false,
    val isLoading: Boolean = true
)

class CommunityDetailViewModel(private val communityId: String) : ViewModel() {
    private val communityRepository = FirebaseDataModule.communityRepository
    private val reportRepository = FirebaseDataModule.reportRepository

    private val _uiState = MutableStateFlow(CommunityDetailUiState())
    val uiState: StateFlow<CommunityDetailUiState> = _uiState.asStateFlow()

    private var allPosts: List<CommunityPost> = emptyList()

    fun setFilter(filter: String) {
        _uiState.value = _uiState.value.copy(
            selectedFilter = filter,
            posts = applyFilter(allPosts, filter)
        )
    }

    private fun applyFilter(posts: List<CommunityPost>, filter: String): List<CommunityPost> {
        return when (filter) {
            "Resolved" -> posts.filter { it.status == PostStatus.RESOLVED }
            "Active" -> posts.filter { it.status != PostStatus.RESOLVED }
            else -> posts
        }
    }

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
                reportRepository.getReportsForCommunity(communityId),
                communityRepository.getNoticesForCommunity(communityId),
                communityRepository.isUserMember(communityId, userId)
            ) { community, posts, reports, notices, isMember ->
                val existingReportIds = posts.mapNotNull { it.reportId }.toSet()
                val extraPostsFromReports = reports.filter { it.id !in existingReportIds }.map { report ->
                    CommunityPost(
                        id = report.id,
                        communityId = report.communityId ?: communityId,
                        authorId = report.reporterId,
                        authorName = report.reporterName,
                        communityName = report.communityName,
                        reportId = report.id,
                        crisisId = report.crisisId,
                        title = report.title,
                        body = report.description,
                        category = report.category,
                        urgency = report.urgency,
                        status = if (report.status == ReportStatus.RESOLVED) PostStatus.RESOLVED else PostStatus.ACTIVE,
                        commentCount = report.commentCount,
                        meTooCount = report.meTooCount,
                        volunteerCount = report.volunteerCount,
                        imageUrls = report.imageUrls,
                        createdAt = report.submittedAt,
                        updatedAt = report.updatedAt
                    )
                }
                CommunityDetailUiState(
                    community = community,
                    posts = (posts + extraPostsFromReports).sortedByDescending { it.createdAt },
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
