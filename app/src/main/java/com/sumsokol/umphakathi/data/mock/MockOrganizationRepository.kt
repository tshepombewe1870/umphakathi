package com.sumsokol.umphakathi.data.mock

import com.sumsokol.umphakathi.domain.model.Organization
import com.sumsokol.umphakathi.domain.model.OrganizationType
import com.sumsokol.umphakathi.domain.repository.OrganizationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class MockOrganizationRepository : OrganizationRepository {
    private val _organizations = MutableStateFlow(sampleOrganizations())

    override fun getOrganizations(): Flow<List<Organization>> = _organizations

    override fun getOrganization(id: String): Flow<Organization?> =
        _organizations.map { list -> list.find { it.id == id } }

    override suspend fun createOrganization(organization: Organization): Result<Organization> {
        val newOrg = organization.copy(id = UUID.randomUUID().toString())
        _organizations.value = _organizations.value + newOrg
        return Result.success(newOrg)
    }

    private fun sampleOrganizations() = listOf(
        Organization(
            id = "org-water",
            accountUserId = "user-water-dept",
            name = "Joburg Water",
            organizationType = OrganizationType.GOVERNMENT_DEPARTMENT,
            verified = true,
            description = "City of Johannesburg Water Department responsible for water supply and waste water services.",
            serviceAreas = listOf("Soweto", "Johannesburg", "Alexandra")
        ),
        Organization(
            id = "org-saps",
            accountUserId = "user-saps",
            name = "SAPS Alexandra",
            organizationType = OrganizationType.EMERGENCY_SERVICES,
            verified = true,
            description = "South African Police Service branch serving Alexandra township and surrounds.",
            serviceAreas = listOf("Alexandra")
        ),
        Organization(
            id = "org-ngo-power",
            accountUserId = "user-020",
            name = "Joburg Infrastructure NGO",
            organizationType = OrganizationType.NGO,
            verified = true,
            description = "Non-profit organization monitoring public power, light infrastructure, and local civic engineering updates.",
            serviceAreas = listOf("Greater Johannesburg")
        )
    )
}
