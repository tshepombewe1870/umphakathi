package com.sumsokol.umphakathi.domain.model

import java.time.Instant

enum class ResolutionMethod {
    REPORTER, COMMUNITY, DEPARTMENT, DEPARTMENT_AND_COMMUNITY
}

data class Resolution(
    val id: String,
    val reportId: String? = null,
    val crisisId: String? = null,
    val postId: String? = null,
    val resolvedBy: String,
    val method: ResolutionMethod,
    val explanation: String,
    val corroborationCount: Int = 0,
    val createdAt: Instant = Instant.now()
)
