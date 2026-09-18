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
    fun getNoticesForCommunity(communityId: String): Flow<List<com.sumsokol.umphakathi.domain.model.Notice>>
    suspend fun createNotice(notice: com.sumsokol.umphakathi.domain.model.Notice): Result<com.sumsokol.umphakathi.domain.model.Notice>
    suspend fun updateNoticeStatus(noticeId: String, status: com.sumsokol.umphakathi.domain.model.NoticeStatus): Result<Unit>
    suspend fun joinCommunity(communityId: String, userId: String): Result<Unit>
    suspend fun leaveCommunity(communityId: String, userId: String): Result<Unit>
    fun isUserMember(communityId: String, userId: String): Flow<Boolean>
}
