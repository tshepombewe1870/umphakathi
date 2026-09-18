package com.sumsokol.umphakathi.domain.model

import java.time.Instant

enum class CrisisStatus {
    IDENTIFIED, ACTIVE, ESCALATED, RESPONSE_IN_PROGRESS, RESOLVED, ARCHIVED
}

data class Crisis(
    val id: String,
    val title: String,
    val description: String,
    val category: ReportCategory,
    val status: CrisisStatus = CrisisStatus.IDENTIFIED,
    val severity: Urgency,
    val urgency: Urgency,
    val potentialHarm: PotentialHarm,
    val incidentLocation: IncidentLocation = IncidentLocation(),
    val reportCount: Int = 0,
    val independentReporterCount: Int = 0,
    val meTooCount: Int = 0,
    val peopleAffected: Int? = null,
    val manpowerRequired: Boolean = false,
    val resourcesRequired: List<String> = emptyList(),
    val responsibleOrganizationId: String? = null,
    val detectedAt: Instant = Instant.now(),
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val resolvedAt: Instant? = null
)
