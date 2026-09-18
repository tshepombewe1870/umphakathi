package com.sumsokol.umphakathi.data.firebase

import com.sumsokol.umphakathi.domain.model.*
import com.sumsokol.umphakathi.domain.repository.CommunityRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant

class FirestoreCommunityRepository(private val db: FirebaseFirestore) : CommunityRepository {

    override fun getCommunities(): Flow<List<Community>> = callbackFlow {
        val subscription = db.collection("communities")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreCommunityRepo", "Error fetching communities: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.documents.mapNotNull { it.toCommunity() })
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getCommunity(id: String): Flow<Community?> = callbackFlow {
        val subscription = db.collection("communities").document(id)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toCommunity())
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getPostsForCommunity(communityId: String): Flow<List<CommunityPost>> = callbackFlow {
        val subscription = db.collection("posts")
            .whereEqualTo("communityId", communityId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreCommunityRepo", "Error fetching posts for community $communityId: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.documents.mapNotNull { it.toPost() }.sortedByDescending { it.createdAt })
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getPostsByReport(reportId: String): Flow<List<CommunityPost>> = callbackFlow {
        val subscription = db.collection("posts")
            .whereEqualTo("reportId", reportId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreCommunityRepo", "Error fetching posts for report $reportId: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.documents.mapNotNull { it.toPost() }.sortedByDescending { it.createdAt })
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getCommentsForPost(postId: String): Flow<List<Comment>> = callbackFlow {
        val subscription = db.collection("comments")
            .whereEqualTo("postId", postId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreCommunityRepo", "Error fetching comments for post $postId: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.documents.mapNotNull { it.toComment() }.sortedBy { it.createdAt })
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun createCommunity(community: Community): Result<Community> = runCatching {
        val docRef = db.collection("communities").document()
        val commWithId = community.copy(id = docRef.id)
        docRef.set(commWithId.toFirestoreMap()).await()
        commWithId
    }

    override suspend fun createPost(post: CommunityPost): Result<CommunityPost> = runCatching {
        val docRef = db.collection("posts").document()
        val postWithId = post.copy(id = docRef.id)
        docRef.set(postWithId.toFirestoreMap()).await()
        postWithId
    }

    override suspend fun addComment(comment: Comment): Result<Comment> = runCatching {
        val docRef = db.collection("comments").document()
        val commentWithId = comment.copy(id = docRef.id)
        docRef.set(commentWithId.toFirestoreMap()).await()
        
        db.collection("posts").document(comment.postId)
            .update("commentCount", com.google.firebase.firestore.FieldValue.increment(1))
        
        commentWithId
    }

    override suspend fun markResolved(postId: String, resolvedBy: String): Result<Unit> = runCatching {
        db.collection("posts").document(postId)
            .update(mapOf(
                "status" to PostStatus.RESOLVED.name,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )).await()
    }
}
