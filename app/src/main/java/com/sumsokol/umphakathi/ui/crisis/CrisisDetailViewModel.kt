package com.sumsokol.umphakathi.ui.crisis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sumsokol.umphakathi.data.firebase.FirebaseDataModule
import com.sumsokol.umphakathi.domain.model.Crisis
import com.sumsokol.umphakathi.domain.model.Report
import com.sumsokol.umphakathi.domain.model.VolunteerOffer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class CrisisDetailUiState(
    val crisis: Crisis? = null,
    val reports: List<Report> = emptyList(),
    val volunteerOffers: List<VolunteerOffer> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class CrisisDetailViewModel(private val crisisId: String) : ViewModel() {
    private val crisisRepository = FirebaseDataModule.crisisRepository
    private val reportRepository = FirebaseDataModule.reportRepository
    private val volunteerRepository = FirebaseDataModule.volunteerRepository

    private val _uiState = MutableStateFlow(CrisisDetailUiState())
    val uiState: StateFlow<CrisisDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                crisisRepository.getCrisis(crisisId),
                reportRepository.getReportsByCrisis(crisisId),
                volunteerRepository.getOffersForCrisis(crisisId)
            ) { crisis, reports, offers ->
                CrisisDetailUiState(
                    crisis = crisis,
                    reports = reports,
                    volunteerOffers = offers,
                    isLoading = false
                )
            }.collect { _uiState.value = it }
        }
    }

    fun submitVolunteerOffer(resourceTypes: List<com.sumsokol.umphakathi.domain.model.ResourceType>, note: String) {
        val userId = FirebaseDataModule.currentUserId ?: return
        viewModelScope.launch {
            // Fetch user name for the offer
            val user = FirebaseDataModule.userRepository.getOrCreateUser(userId)
            
            resourceTypes.forEach { type ->
                volunteerRepository.submitOffer(
                    com.sumsokol.umphakathi.domain.model.VolunteerOffer(
                        id = "",
                        userId = userId,
                        userName = user.username,
                        crisisId = crisisId,
                        resourceType = type,
                        quantity = 1,
                        note = note,
                        status = com.sumsokol.umphakathi.domain.model.VolunteerStatus.OFFERED
                    )
                )
            }
        }
    }

    companion object {
        fun factory(crisisId: String) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CrisisDetailViewModel(crisisId) as T
        }
    }
}
