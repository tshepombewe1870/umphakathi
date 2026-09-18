package com.sumsokol.umphakathi.domain.model

import java.time.Instant

enum class EvidenceType { IMAGE, VIDEO, DOCUMENT, AUDIO }

data class Evidence(
    val id: String,
    val reportId: String? = null,
    val crisisId: String? = null,
    val postId: String? = null,
    val uploadedBy: String,
    val type: EvidenceType,
    val storagePath: String,
    val createdAt: Instant = Instant.now()
)
