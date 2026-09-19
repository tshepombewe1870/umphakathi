package com.sumsokol.umphakathi.ui.organization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sumsokol.umphakathi.data.firebase.FirebaseDataModule
import com.sumsokol.umphakathi.domain.model.Organization
import com.sumsokol.umphakathi.domain.model.Report
import com.sumsokol.umphakathi.domain.model.ReportCategory
import com.sumsokol.umphakathi.domain.model.ReportStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class OrganizationDetailUiState(
    val organization: Organization? = null,
    val reports: List<Report> = emptyList(),
    val selectedFilter: String = "All",
    val allCount: Int = 0,
    val assignedCount: Int = 0,
    val resolvedCount: Int = 0,
    val activeCount: Int = 0,
    val isLoading: Boolean = true
)

class OrganizationDetailViewModel(private val organizationId: String) : ViewModel() {
    private val organizationRepository = FirebaseDataModule.organizationRepository
    private val reportRepository = FirebaseDataModule.reportRepository

    private val _uiState = MutableStateFlow(OrganizationDetailUiState())
    val uiState: StateFlow<OrganizationDetailUiState> = _uiState.asStateFlow()

    private var allFilteredReports: List<Report> = emptyList()

    fun setFilter(filter: String) {
        val currentOrg = _uiState.value.organization ?: return
        _uiState.value = _uiState.value.copy(
            selectedFilter = filter,
            reports = applyStatFilter(allFilteredReports, filter, currentOrg.id)
        )
    }

    private fun applyStatFilter(reports: List<Report>, filter: String, orgId: String): List<Report> {
        return when (filter) {
            "All" -> reports
            "Assigned" -> reports.filter { it.responsibleOrganizationId == orgId }
            "Resolved" -> reports.filter { it.status == ReportStatus.RESOLVED }
            "Active" -> reports.filter { it.status != ReportStatus.RESOLVED }
            else -> reports
        }
    }

    init {
        viewModelScope.launch {
            organizationRepository.getOrganization(organizationId).collect { org ->
                if (org != null) {
                    reportRepository.getReports().collect { allReports ->
                        // Broaden the filter to capture assigned, matched category mandates, or location tags
                        val filteredReports = allReports.filter { report ->
                            val isAssigned = report.responsibleOrganizationId == org.id
                            
                            // Match based on category mandate alignment
                            val matchesMandate = when {
                                org.name.contains("Water", ignoreCase = true) -> 
                                    report.category == ReportCategory.WATER_SEWAGE || report.category == ReportCategory.UNSAFE_ENVIRONMENT
                                org.name.contains("SAPS", ignoreCase = true) || org.name.contains("Police", ignoreCase = true) ->
                                    report.category == ReportCategory.CRIME || report.category == ReportCategory.ABUSE || report.category == ReportCategory.VIOLENCE || report.category == ReportCategory.MISSING_PERSON
                                org.name.contains("Power", ignoreCase = true) || org.name.contains("Infrastructure", ignoreCase = true) ->
                                    report.category == ReportCategory.INFRASTRUCTURE || report.category == ReportCategory.OTHER
                                else -> false
                            }

                            // Match based on region service areas
                            val matchesRegion = org.serviceAreas.isEmpty() || org.serviceAreas.any { area ->
                                report.incidentLocation.neighborhood?.contains(area, ignoreCase = true) == true ||
                                report.incidentLocation.city?.contains(area, ignoreCase = true) == true ||
                                report.communityName?.contains(area, ignoreCase = true) == true
                            }

                            isAssigned || (matchesMandate && matchesRegion)
                        }
                        allFilteredReports = filteredReports
                        val countAll = filteredReports.size
                        val countAssigned = filteredReports.count { it.responsibleOrganizationId == org.id }
                        val rCount = filteredReports.count { it.status == ReportStatus.RESOLVED }
                        val aCount = filteredReports.count { it.status != ReportStatus.RESOLVED }

                        _uiState.value = OrganizationDetailUiState(
                            organization = org,
                            reports = applyStatFilter(filteredReports, "All", org.id),
                            selectedFilter = "All",
                            allCount = countAll,
                            assignedCount = countAssigned,
                            resolvedCount = rCount,
                            activeCount = aCount,
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
