package com.sumsokol.umphakathi.ui.organization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumsokol.umphakathi.data.firebase.FirebaseDataModule
import com.sumsokol.umphakathi.domain.model.Organization
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OrganizationUiState(
    val organizations: List<Organization> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class OrganizationViewModel : ViewModel() {
    private val organizationRepository = FirebaseDataModule.organizationRepository

    private val _uiState = MutableStateFlow(OrganizationUiState())
    val uiState: StateFlow<OrganizationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            organizationRepository.getOrganizations().collect { organizations ->
                _uiState.value = _uiState.value.copy(organizations = organizations, isLoading = false)
            }
        }
    }
}
