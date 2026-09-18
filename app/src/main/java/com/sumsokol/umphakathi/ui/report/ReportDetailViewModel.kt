package com.sumsokol.umphakathi.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sumsokol.umphakathi.data.firebase.FirebaseDataModule
import com.sumsokol.umphakathi.domain.model.Comment
import com.sumsokol.umphakathi.domain.model.Report
import com.sumsokol.umphakathi.domain.model.ReportStatus
import com.sumsokol.umphakathi.domain.model.VolunteerOffer
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.Instant

data class ReportDetailUiState(
    val report: Report? = null,
    val volunteerOffers: List<VolunteerOffer> = emptyList(),
    val officialUpdates: List<com.sumsokol.umphakathi.domain.model.OfficialUpdate> = emptyList(),
    val auditEvents: List<com.sumsokol.umphakathi.domain.model.AuditEvent> = emptyList(),
    val corroborations: List<com.sumsokol.umphakathi.domain.model.ReportExperience> = emptyList(),
    val shares: List<com.sumsokol.umphakathi.domain.model.CommunityPost> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = true,
    val userHasMeTood: Boolean = false,
    val error: String? = null
)

class ReportDetailViewModel(private val reportId: String) : ViewModel() {
    private val reportRepository = FirebaseDataModule.reportRepository
    private val volunteerRepository = FirebaseDataModule.volunteerRepository
    private val communityRepository = FirebaseDataModule.communityRepository

    private val _uiState = MutableStateFlow(ReportDetailUiState())
    val uiState: StateFlow<ReportDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                reportRepository.getReport(reportId),
                volunteerRepository.getOffersForReport(reportId),
                reportRepository.getOfficialUpdates(reportId),
                reportRepository.getAuditEvents(reportId),
                reportRepository.getExperiences(reportId),
                communityRepository.getPostsByReport(reportId),
                reportRepository.getComments(reportId)
            ) { array ->
                @Suppress("UNCHECKED_CAST")
                val report = array[0] as com.sumsokol.umphakathi.domain.model.Report?
                @Suppress("UNCHECKED_CAST")
                val offers = array[1] as List<com.sumsokol.umphakathi.domain.model.VolunteerOffer>
                @Suppress("UNCHECKED_CAST")
                val updates = array[2] as List<com.sumsokol.umphakathi.domain.model.OfficialUpdate>
                @Suppress("UNCHECKED_CAST")
                val audits = array[3] as List<com.sumsokol.umphakathi.domain.model.AuditEvent>
                @Suppress("UNCHECKED_CAST")
                val corroborations = array[4] as List<com.sumsokol.umphakathi.domain.model.ReportExperience>
                @Suppress("UNCHECKED_CAST")
                val shares = array[5] as List<com.sumsokol.umphakathi.domain.model.CommunityPost>
                @Suppress("UNCHECKED_CAST")
                val comments = array[6] as List<Comment>

                ReportDetailUiState(
                    report = report,
                    volunteerOffers = offers,
                    officialUpdates = updates,
                    auditEvents = audits,
                    corroborations = corroborations,
                    shares = shares,
                    comments = comments,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun addMeToo(description: String? = null) {
        val userId = FirebaseDataModule.currentUserId ?: return
        viewModelScope.launch {
            reportRepository.addMeToo(reportId, userId, description)
        }
    }

    fun submitVolunteerOffer(resourceTypes: List<com.sumsokol.umphakathi.domain.model.ResourceType>, note: String) {
        val userId = FirebaseDataModule.currentUserId ?: return
        viewModelScope.launch {
            // Fetch user name for the offer
            val user = FirebaseDataModule.userRepository.getOrCreateUser(userId)

            resourceTypes.forEach { type ->
                volunteerRepository.submitOffer(
                    VolunteerOffer(
                        id = "",
                        userId = userId,
                        userName = user.username,
                        reportId = reportId,
                        resourceType = type,
                        quantity = 1,
                        note = note,
                        status = com.sumsokol.umphakathi.domain.model.VolunteerStatus.OFFERED
                    )
                )
            }
        }
    }

    fun flagAsCrisis() {
        val userId = FirebaseDataModule.currentUserId ?: return
        viewModelScope.launch {
            reportRepository.flagAsCrisis(reportId, userId)
        }
    }

    fun shareReport() {
        val userId = FirebaseDataModule.currentUserId ?: return
        viewModelScope.launch {
            reportRepository.shareReport(reportId, userId)
        }
    }

    fun resolveReport(explanation: String) {
        val userId = FirebaseDataModule.currentUserId ?: return
        viewModelScope.launch {
            val resolution = com.sumsokol.umphakathi.domain.model.Resolution(
                id = java.util.UUID.randomUUID().toString(),
                reportId = reportId,
                resolvedBy = userId,
                method = com.sumsokol.umphakathi.domain.model.ResolutionMethod.REPORTER,
                explanation = explanation
            )
            reportRepository.resolveReport(reportId, resolution)
        }
    }

    fun postOfficialUpdate(message: String, statusUpdate: ReportStatus?) {
        val userId = FirebaseDataModule.currentUserId ?: return
        viewModelScope.launch {
            val user = FirebaseDataModule.userRepository.getOrCreateUser(userId)
            if (user.accountType.name != "ORGANIZATION") return@launch

            val update = com.sumsokol.umphakathi.domain.model.OfficialUpdate(
                id = "",
                reportId = reportId,
                organizationId = userId,
                organizationName = user.communityName ?: user.username,
                message = message,
                statusUpdate = statusUpdate
            )
            reportRepository.addOfficialUpdate(update)
        }
    }

    fun toggleLikeOffer(offerId: String) {
        val userId = FirebaseDataModule.currentUserId ?: return
        viewModelScope.launch {
            volunteerRepository.toggleLikeOffer(offerId, userId)
        }
    }

    fun addVolunteerComment(offerId: String, body: String) {
        val userId = FirebaseDataModule.currentUserId ?: return
        viewModelScope.launch {
            val user = FirebaseDataModule.userRepository.getOrCreateUser(userId)
            volunteerRepository.addComment(
                com.sumsokol.umphakathi.domain.model.Comment(
                    id = "",
                    postId = offerId, // using postId for offerId in comments
                    authorId = userId,
                    authorName = user.username,
                    body = body,
                    createdAt = Instant.now()
                )
            )
        }
    }

    /**
     * Comments on the report itself (distinct from addVolunteerComment, which comments
     * on a specific volunteer offer). Powers the Community tab + CommentBottomSheet.
     */
    fun addComment(body: String, parentCommentId: String? = null) {
        val userId = FirebaseDataModule.currentUserId ?: return
        viewModelScope.launch {
            val user = FirebaseDataModule.userRepository.getOrCreateUser(userId)
            reportRepository.addComment(
                Comment(
                    id = "",
                    postId = reportId,
                    authorId = userId,
                    authorName = user.username,
                    parentCommentId = parentCommentId,
                    body = body,
                    createdAt = Instant.now()
                )
            )
        }
    }

    companion object {
        fun factory(reportId: String) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ReportDetailViewModel(reportId) as T
        }
    }
}
