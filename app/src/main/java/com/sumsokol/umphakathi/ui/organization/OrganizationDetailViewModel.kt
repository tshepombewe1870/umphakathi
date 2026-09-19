package com.sumsokol.umphakathi.ui.organization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sumsokol.umphakathi.data.firebase.FirebaseDataModule
import com.sumsokol.umphakathi.domain.model.Organization
import com.sumsokol.umphakathi.domain.model.Report
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class OrganizationDetailUiState(
    val organization: Organization? = null,
    val reports: List<Report> = emptyList(),
    val isLoading: Boolean = true
)

class OrganizationDetailViewModel(private val organizationId: String) : ViewModel() {
    private val organizationRepository = FirebaseDataModule.organizationRepository
    private val reportRepository = FirebaseDataModule.reportRepository

    private val _uiState = MutableStateFlow(OrganizationDetailUiState())
    val uiState: StateFlow<OrganizationDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            organizationRepository.getOrganization(organizationId).collect { org ->
                if (org != null) {
                    reportRepository.getReports().collect { allReports ->
                        val filteredReports = allReports.filter { it.responsibleOrganizationId == org.id }
                        _uiState.value = OrganizationDetailUiState(
                            organization = org,
                            reports = filteredReports.sortedByDescending { it.submittedAt },
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    companion object {
        fun factory(organizationId: String) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                OrganizationDetailViewModel(organizationId) as T
        }
    }
}
