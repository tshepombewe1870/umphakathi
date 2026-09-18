package com.sumsokol.umphakathi.domain.repository

import com.sumsokol.umphakathi.domain.model.Comment
import com.sumsokol.umphakathi.domain.model.Community
import com.sumsokol.umphakathi.domain.model.CommunityPost
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {
    fun getCommunities(): Flow<List<Community>>
    fun getCommunity(id: String): Flow<Community?>
    fun getPostsForCommunity(communityId: String): Flow<List<CommunityPost>>
    fun getPostsByReport(reportId: String): Flow<List<CommunityPost>>
    fun getCommentsForPost(postId: String): Flow<List<Comment>>
    suspend fun createCommunity(community: Community): Result<Community>
    suspend fun createPost(post: CommunityPost): Result<CommunityPost>
    suspend fun addComment(comment: Comment): Result<Comment>
    suspend fun markResolved(postId: String, resolvedBy: String): Result<Unit>
}
