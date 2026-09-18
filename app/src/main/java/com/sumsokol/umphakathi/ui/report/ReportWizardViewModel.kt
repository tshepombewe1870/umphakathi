package com.sumsokol.umphakathi.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumsokol.umphakathi.data.firebase.FirebaseDataModule
import com.sumsokol.umphakathi.domain.model.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class WizardStep { CATEGORY, DESCRIPTION, LOCATION, URGENCY, REVIEW }

data class ReportDraft(
    val category: ReportCategory? = null,
    val title: String = "",
    val description: String = "",
    val locationSource: LocationSource = LocationSource.NOT_PROVIDED,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val addressDescription: String = "",
    val city: String = "",
    val neighborhood: String = "",
    val urgency: Urgency = Urgency.MEDIUM,
    val potentialHarm: PotentialHarm = PotentialHarm.MODERATE,
    val locationVisibility: LocationVisibility = LocationVisibility.PUBLIC_APPROXIMATE,
    val communityId: String? = null
)

data class WizardUiState(
    val step: WizardStep = WizardStep.CATEGORY,
    val draft: ReportDraft = ReportDraft(),
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null
)

class ReportWizardViewModel(private val communityId: String? = null) : ViewModel() {
    private val reportRepository = FirebaseDataModule.reportRepository

    private val _uiState = MutableStateFlow(WizardUiState(draft = ReportDraft(communityId = communityId)))
    val uiState: StateFlow<WizardUiState> = _uiState.asStateFlow()

    fun selectCategory(category: ReportCategory) {
        _uiState.value = _uiState.value.copy(
            draft = _uiState.value.draft.copy(category = category),
            step = WizardStep.DESCRIPTION
        )
    }

    fun setDescription(title: String, description: String) {
        _uiState.value = _uiState.value.copy(
            draft = _uiState.value.draft.copy(title = title, description = description),
            step = WizardStep.LOCATION
        )
    }

    fun setLocation(source: LocationSource, lat: Double? = null, lng: Double? = null, address: String = "", city: String = "", neighborhood: String = "") {
        _uiState.value = _uiState.value.copy(
            draft = _uiState.value.draft.copy(
                locationSource = source,
                latitude = lat,
                longitude = lng,
                addressDescription = address,
                city = city,
                neighborhood = neighborhood
            ),
            step = WizardStep.URGENCY
        )
    }

    fun skipLocation() {
        setLocation(LocationSource.NOT_PROVIDED)
    }

    fun setUrgencyAndHarm(urgency: Urgency, potentialHarm: PotentialHarm) {
        _uiState.value = _uiState.value.copy(
            draft = _uiState.value.draft.copy(urgency = urgency, potentialHarm = potentialHarm),
            step = WizardStep.REVIEW
        )
    }

    fun goBack() {
        val currentStep = _uiState.value.step
        val prevStep = when (currentStep) {
            WizardStep.DESCRIPTION -> WizardStep.CATEGORY
            WizardStep.LOCATION -> WizardStep.DESCRIPTION
            WizardStep.URGENCY -> WizardStep.LOCATION
            WizardStep.REVIEW -> WizardStep.URGENCY
            else -> return
        }
        _uiState.value = _uiState.value.copy(step = prevStep)
    }

    fun submitReport() {
        val draft = _uiState.value.draft
        val category = draft.category ?: return
        val userId = FirebaseDataModule.currentUserId ?: return

        _uiState.value = _uiState.value.copy(isSubmitting = true)

        viewModelScope.launch {
            val report = Report(
                id = UUID.randomUUID().toString(),
                reporterId = userId,
                reporterName = "Demo User", // Should ideally fetch from a user registry but good for mock
                communityId = draft.communityId,
                title = draft.title,
                description = draft.description,
                category = category,
                urgency = draft.urgency,
                potentialHarm = draft.potentialHarm,
                incidentLocation = IncidentLocation(
                    source = draft.locationSource,
                    latitude = draft.latitude,
                    longitude = draft.longitude,
                    addressDescription = draft.addressDescription.ifBlank { null },
                    city = draft.city.ifBlank { null },
                    neighborhood = draft.neighborhood.ifBlank { null }
                ),
                locationVisibility = draft.locationVisibility
            )
            val result = reportRepository.submitReport(report)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, isSubmitted = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to submit report"
                )
            }
        }
    }

    companion object {
        fun factory(communityId: String?) = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ReportWizardViewModel(communityId) as T
        }
    }
}
