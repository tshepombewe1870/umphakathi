package com.sumsokol.umphakathi.domain.repository

import com.sumsokol.umphakathi.domain.model.Organization
import kotlinx.coroutines.flow.Flow

interface OrganizationRepository {
    fun getOrganizations(): Flow<List<Organization>>
    fun getOrganization(id: String): Flow<Organization?>
    suspend fun createOrganization(organization: Organization): Result<Organization>
}
