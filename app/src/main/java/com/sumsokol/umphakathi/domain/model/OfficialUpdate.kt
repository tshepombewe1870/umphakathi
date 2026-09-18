package com.sumsokol.umphakathi.domain.model

import java.time.Instant

data class OfficialUpdate(
    val id: String,
    val reportId: String,
    val organizationId: String,
    val organizationName: String,
    val message: String,
    val statusUpdate: ReportStatus? = null,
    val createdAt: Instant = Instant.now()
)
