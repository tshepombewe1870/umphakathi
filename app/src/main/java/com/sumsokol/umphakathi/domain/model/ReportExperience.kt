package com.sumsokol.umphakathi.domain.model

import java.time.Instant

data class ReportExperience(
    val id: String,
    val reportId: String,
    val userId: String,
    val userName: String = "Anonymous",
    val description: String? = null,
    val incidentStartedAt: Instant? = null,
    val createdAt: Instant = Instant.now()
)
