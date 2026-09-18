package com.sumsokol.umphakathi.data.mock

import com.sumsokol.umphakathi.domain.model.*
import com.sumsokol.umphakathi.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class MockCommunityRepository : CommunityRepository {
    private val _communities = MutableStateFlow(sampleCommunities())
    private val _posts = MutableStateFlow(samplePosts())
    private val _comments = MutableStateFlow(sampleComments())

    override fun getCommunities(): Flow<List<Community>> = _communities

    override fun getCommunity(id: String): Flow<Community?> =
        _communities.map { list -> list.find { it.id == id } }

    override fun getPostsForCommunity(communityId: String): Flow<List<CommunityPost>> =
        _posts.map { list -> list.filter { it.communityId == communityId } }

    override fun getPostsByReport(reportId: String): Flow<List<CommunityPost>> =
        _posts.map { list -> list.filter { it.reportId == reportId } }

    override fun getCommentsForPost(postId: String): Flow<List<Comment>> =
        _comments.map { list -> list.filter { it.postId == postId } }

    override suspend fun createCommunity(community: Community): Result<Community> {
        val newCommunity = community.copy(id = UUID.randomUUID().toString())
        _communities.value = _communities.value + newCommunity
        return Result.success(newCommunity)
    }

    override suspend fun createPost(post: CommunityPost): Result<CommunityPost> {
        val newPost = post.copy(id = UUID.randomUUID().toString(), createdAt = Instant.now())
        _posts.value = _posts.value + newPost
        return Result.success(newPost)
    }

    override suspend fun addComment(comment: Comment): Result<Comment> {
        val newComment = comment.copy(id = UUID.randomUUID().toString(), createdAt = Instant.now())
        _comments.value = _comments.value + newComment
        _posts.value = _posts.value.map {
            if (it.id == comment.postId) it.copy(commentCount = it.commentCount + 1) else it
        }
        return Result.success(newComment)
    }

    override suspend fun markResolved(postId: String, resolvedBy: String): Result<Unit> {
        _posts.value = _posts.value.map {
            if (it.id == postId) it.copy(status = PostStatus.RESOLVED) else it
        }
        return Result.success(Unit)
    }

    private fun sampleCommunities() = listOf(
        Community(
            id = "community-001",
            name = "Soweto Community",
            description = "A community group for Soweto residents to report and coordinate responses to local issues.",
            type = CommunityType.GEOGRAPHIC,
            creatorId = "user-001",
            ownerId = "user-001",
            moderatorIds = listOf("user-002", "user-003"),
            location = "Soweto, Johannesburg",
            memberCount = 1243,
            createdAt = Instant.now().minus(180, ChronoUnit.DAYS)
        ),
        Community(
            id = "community-002",
            name = "Alexandra Township Watch",
            description = "Community safety and infrastructure reporting for Alexandra Township.",
            type = CommunityType.GEOGRAPHIC,
            creatorId = "user-010",
            ownerId = "user-010",
            memberCount = 876,
            createdAt = Instant.now().minus(90, ChronoUnit.DAYS)
        ),
        Community(
            id = "community-003",
            name = "Joburg Infrastructure NGO",
            description = "NGO focused on infrastructure maintenance and reporting in greater Johannesburg.",
            type = CommunityType.ORGANIZATION,
            creatorId = "user-020",
            ownerId = "user-020",
            memberCount = 342,
            createdAt = Instant.now().minus(365, ChronoUnit.DAYS)
        )
    )

    private fun samplePosts() = listOf(
        CommunityPost(
            id = "post-001",
            communityId = "community-001",
            authorId = "user-111",
            reportId = "report-001",
            crisisId = "crisis-001",
            title = "UPDATE: Water tankers deployed to Soweto",
            body = "The City of Johannesburg Water Department has confirmed that 8 water tankers have been deployed to the affected areas. Please check the pinned locations for tanker positions.",
            status = PostStatus.ACTIVE,
            commentCount = 23,
            meTooCount = 67,
            volunteerCount = 5,
            authorName = "Thabo N.",
            communityName = "Soweto Community",
            imageUrls = listOf("https://images.unsplash.com/photo-1541888946425-d81bb19240f5", "https://images.unsplash.com/photo-1590069261209-f8e9b8642343"),
            createdAt = Instant.now().minus(8, ChronoUnit.HOURS)
        ),
        CommunityPost(
            id = "post-002",
            communityId = "community-001",
            authorId = "user-222",
            title = "Volunteers needed to help elderly residents",
            body = "With the water crisis ongoing, many elderly residents in our community need help collecting water from the tanker points. If you have transport or time, please volunteer.",
            status = PostStatus.ACTIVE,
            commentCount = 11,
            meTooCount = 4,
            volunteerCount = 12,
            authorName = "Lerato M.",
            communityName = "Soweto Community",
            createdAt = Instant.now().minus(6, ChronoUnit.HOURS)
        ),
        CommunityPost(
            id = "post-003",
            communityId = "community-002",
            authorId = "user-333",
            reportId = "report-004",
            crisisId = "crisis-002",
            title = "Search party organizing for missing teen",
            body = "We are organizing a community search party starting at 6am tomorrow. Meet at the community hall. Please share this post widely.",
            status = PostStatus.ACTIVE,
            commentCount = 45,
            meTooCount = 0,
            volunteerCount = 34,
            authorName = "Officer Sithole",
            communityName = "Alexandra Township Watch",
            imageUrls = listOf("https://images.unsplash.com/photo-1504159506876-f8338247a14a"),
            createdAt = Instant.now().minus(20, ChronoUnit.HOURS)
        ),
        CommunityPost(
            id = "post-004",
            communityId = "community-002",
            authorId = "user-444",
            reportId = "report-003",
            title = "N12 Pothole still not fixed after 5 days",
            body = "This dangerous pothole near Crown Mines has been reported multiple times. Three cars had blowouts last night. We need to escalate this to the roads department urgently.",
            status = PostStatus.ACTIVE,
            commentCount = 18,
            meTooCount = 27,
            volunteerCount = 0,
            authorName = "David K.",
            communityName = "Alexandra Township Watch",
            createdAt = Instant.now().minus(3, ChronoUnit.DAYS)
        )
    )

    private fun sampleComments() = listOf(
        Comment(
            id = "comment-001",
            postId = "post-001",
            authorId = "user-555",
            body = "Thank you for this update! Which areas are getting tankers first?",
            createdAt = Instant.now().minus(7, ChronoUnit.HOURS)
        ),
        Comment(
            id = "comment-002",
            postId = "post-001",
            authorId = "user-111",
            parentCommentId = "comment-001",
            body = "Orlando West and Meadowlands first, then Diepkloof and Dobsonville by midday.",
            createdAt = Instant.now().minus(6, ChronoUnit.HOURS)
        ),
        Comment(
            id = "comment-003",
            postId = "post-002",
            authorId = "user-666",
            body = "I have a bakkie and can help - how do I sign up?",
            createdAt = Instant.now().minus(5, ChronoUnit.HOURS)
        )
    )
}
