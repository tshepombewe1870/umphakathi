package com.sumsokol.umphakathi.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumsokol.umphakathi.data.firebase.FirebaseDataModule
import com.sumsokol.umphakathi.domain.model.Comment
import com.sumsokol.umphakathi.domain.model.Crisis
import com.sumsokol.umphakathi.domain.model.Report
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

data class ReportsUiState(
    val reports: List<Report> = emptyList(),
    val crises: List<Crisis> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ReportsViewModel : ViewModel() {
    private val reportRepository = FirebaseDataModule.reportRepository
    private val crisisRepository = FirebaseDataModule.crisisRepository

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                reportRepository.getReports(),
                crisisRepository.getCrises()
            ) { reports, crises ->
                ReportsUiState(
                    reports = reports,
                    crises = crises,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun addMeToo(reportId: String) {
        val userId = FirebaseDataModule.currentUserId ?: return
        viewModelScope.launch {
            reportRepository.addMeToo(reportId, userId, null)
        }
    }

    fun submitVolunteerOffer(reportId: String, resourceTypes: List<com.sumsokol.umphakathi.domain.model.ResourceType>, note: String) {
        val userId = FirebaseDataModule.currentUserId ?: return
        viewModelScope.launch {
            val user = FirebaseDataModule.userRepository.getOrCreateUser(userId)
            
            resourceTypes.forEach { type ->
                FirebaseDataModule.volunteerRepository.submitOffer(
                    com.sumsokol.umphakathi.domain.model.VolunteerOffer(
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

    fun flagAsCrisis(reportId: String) {
        val userId = FirebaseDataModule.currentUserId ?: return
        viewModelScope.launch {
            reportRepository.flagAsCrisis(reportId, userId)
        }
    }

    fun shareReport(reportId: String) {
        val userId = FirebaseDataModule.currentUserId ?: return
        viewModelScope.launch {
            reportRepository.shareReport(reportId, userId)
        }
    }

    fun getComments(reportId: String) = reportRepository.getComments(reportId)

    fun addComment(reportId: String, body: String, parentId: String? = null) {
        val userId = FirebaseDataModule.currentUserId ?: return
        viewModelScope.launch {
            // In a real app, we'd fetch the user's name first. 
            // For now, the backend mapping or a named arg will handle it.
            reportRepository.addComment(
                Comment(
                    id = UUID.randomUUID().toString(),
                    postId = reportId,
                    authorId = userId,
                    parentCommentId = parentId,
                    body = body,
                    createdAt = Instant.now()
                )
            )
        }
    }
}
