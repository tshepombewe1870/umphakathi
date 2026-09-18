package com.sumsokol.umphakathi.domain.model

import java.time.Instant

data class Comment(
    val id: String,
    val postId: String,
    val authorId: String,
    val authorName: String = "Anonymous",
    val parentCommentId: String? = null,
    val body: String,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val isDeleted: Boolean = false
)
