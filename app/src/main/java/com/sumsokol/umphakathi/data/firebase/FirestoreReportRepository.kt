package com.sumsokol.umphakathi.data.firebase

import com.sumsokol.umphakathi.domain.model.*
import com.sumsokol.umphakathi.domain.repository.ReportRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant

class FirestoreReportRepository(private val db: FirebaseFirestore) : ReportRepository {

    override fun getReports(): Flow<List<Report>> = callbackFlow {
        val subscription = db.collection("reports")
            .orderBy("submittedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreReportRepo", "Error fetching reports: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.documents.mapNotNull { it.toReport() })
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getReport(id: String): Flow<Report?> = callbackFlow {
        val subscription = db.collection("reports").document(id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreReportRepo", "Error fetching report $id: ${error.message}")
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toReport())
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getReportsByCrisis(crisisId: String): Flow<List<Report>> = callbackFlow {
        val subscription = db.collection("reports")
            .whereEqualTo("crisisId", crisisId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreReportRepo", "Error fetching reports for crisis $crisisId: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.documents.mapNotNull { it.toReport() })
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getReportsByStatus(status: ReportStatus): Flow<List<Report>> = callbackFlow {
        val subscription = db.collection("reports")
            .whereEqualTo("status", status.name)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.documents.mapNotNull { it.toReport() })
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getReportsForCommunity(communityId: String): Flow<List<Report>> = callbackFlow {
        val subscription = db.collection("reports")
            .whereEqualTo("communityId", communityId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreReportRepo", "Error fetching reports for community $communityId: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.documents.mapNotNull { it.toReport() })
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun submitReport(report: Report): Result<Report> = runCatching {
        val docRef = db.collection("reports").document()
        val reportWithId = report.copy(id = docRef.id)
        docRef.set(reportWithId.toFirestoreMap()).await()
        reportWithId
    }

    override suspend fun updateReport(report: Report): Result<Report> = runCatching {
        db.collection("reports").document(report.id).set(report.toFirestoreMap()).await()
        report
    }

    override suspend fun addMeToo(reportId: String, userId: String, description: String?): Result<Unit> = runCatching {
        val reportRef = db.collection("reports").document(reportId)
        val userRef = db.collection("users").document(userId)
        
        db.runTransaction { transaction ->
            val reportSnapshot = transaction.get(reportRef)
            val userSnapshot = transaction.get(userRef)
            
            val user = userSnapshot.toUser() ?: throw Exception("User not found")
            val currentCount = reportSnapshot.getLong("meTooCount") ?: 0
            transaction.update(reportRef, "meTooCount", currentCount + 1)
            
            val experienceRef = db.collection("experiences").document()
            val experience = ReportExperience(
                id = experienceRef.id,
                reportId = reportId,
                userId = userId,
                userName = user.username,
                description = description,
                createdAt = Instant.now()
            )
            transaction.set(experienceRef, experience.toFirestoreMap())
        }.await()
    }

    override suspend fun toggleLikeReport(reportId: String, userId: String): Result<Unit> = runCatching {
        val likeRef = db.collection("reportLikes").document("${reportId}_${userId}")
        val reportRef = db.collection("reports").document(reportId)
        
        db.runTransaction { transaction ->
            val likeSnapshot = transaction.get(likeRef)
            val reportSnapshot = transaction.get(reportRef)
            val currentLikes = reportSnapshot.getLong("likeCount") ?: 0
            
            if (likeSnapshot.exists()) {
                transaction.delete(likeRef)
                transaction.update(reportRef, "likeCount", (currentLikes - 1).coerceAtLeast(0))
            } else {
                transaction.set(likeRef, mapOf("reportId" to reportId, "userId" to userId, "createdAt" to com.google.firebase.Timestamp.now()))
                transaction.update(reportRef, "likeCount", currentLikes + 1)
            }
        }.await()
    }

    override suspend fun toggleLikeComment(commentId: String, userId: String): Result<Unit> = runCatching {
        val likeRef = db.collection("commentLikes").document("${commentId}_${userId}")
        val commentRef = db.collection("comments").document(commentId)
        
        db.runTransaction { transaction ->
            val likeSnapshot = transaction.get(likeRef)
            val commentSnapshot = transaction.get(commentRef)
            val currentLikes = commentSnapshot.getLong("likeCount") ?: 0
            
            if (likeSnapshot.exists()) {
                transaction.delete(likeRef)
                transaction.update(commentRef, "likeCount", (currentLikes - 1).coerceAtLeast(0))
            } else {
                transaction.set(likeRef, mapOf("commentId" to commentId, "userId" to userId, "createdAt" to com.google.firebase.Timestamp.now()))
                transaction.update(commentRef, "likeCount", currentLikes + 1)
            }
        }.await()
    }

    override suspend fun flagAsCrisis(reportId: String, userId: String): Result<Unit> = runCatching {
        val signalRef = db.collection("crisisSignals").document()
        signalRef.set(mapOf(
            "id" to signalRef.id,
            "reportId" to reportId,
            "userId" to userId,
            "type" to "COMMUNITY_FLAG",
            "createdAt" to com.google.firebase.Timestamp.now()
        )).await()
        
        db.collection("reports").document(reportId)
            .update("status", ReportStatus.ESCALATED.name).await()
    }

    override suspend fun resolveReport(reportId: String, resolution: Resolution): Result<Unit> = runCatching {
        val resRef = db.collection("resolutions").document()
        val resWithId = resolution.copy(id = resRef.id)
        resRef.set(resWithId.toFirestoreMap()).await()
        
        db.collection("reports").document(reportId)
            .update(mapOf(
                "status" to ReportStatus.RESOLVED.name,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )).await()
    }

    override fun getComments(reportId: String): Flow<List<Comment>> = callbackFlow {
        val subscription = db.collection("comments")
            .whereEqualTo("reportId", reportId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreReportRepo", "Error fetching comments for report $reportId: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.documents.mapNotNull { it.toComment() }.sortedBy { it.createdAt })
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun addComment(comment: Comment): Result<Comment> = runCatching {
        val docRef = db.collection("comments").document()
        val commentWithId = comment.copy(id = docRef.id)
        docRef.set(commentWithId.toFirestoreMap()).await()
        commentWithId
    }

    override fun getOfficialUpdates(reportId: String): Flow<List<OfficialUpdate>> = callbackFlow {
        val subscription = db.collection("officialUpdates")
            .whereEqualTo("reportId", reportId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreReportRepo", "Error fetching official updates for report $reportId: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.documents.mapNotNull { it.toOfficialUpdate() }.sortedByDescending { it.createdAt })
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun addOfficialUpdate(update: OfficialUpdate): Result<OfficialUpdate> = runCatching {
        val docRef = db.collection("officialUpdates").document()
        val updateWithId = update.copy(id = docRef.id)
        docRef.set(updateWithId.toFirestoreMap()).await()
        
        // If a status update is included, update the report status too
        update.statusUpdate?.let { status ->
            db.collection("reports").document(update.reportId)
                .update("status", status.name)
        }

        // Add to incident timeline (AuditEvents)
        val auditRef = db.collection("auditEvents").document()
        val auditEvent = AuditEvent(
            id = auditRef.id,
            entityType = "REPORT",
            entityId = update.reportId,
            actorId = update.organizationId,
            action = AuditAction.OFFICIAL_UPDATE_ADDED,
            metadata = mapOf(
                "organizationName" to update.organizationName,
                "message" to update.message.take(100),
                "statusUpdate" to (update.statusUpdate?.name ?: "")
            ),
            createdAt = Instant.now()
        )
        auditRef.set(auditEvent.toFirestoreMap()).await()
        
        updateWithId
    }

    override fun getAuditEvents(reportId: String): Flow<List<AuditEvent>> = callbackFlow {
        val subscription = db.collection("auditEvents")
            .whereEqualTo("entityId", reportId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreReportRepo", "Error fetching audit events for report $reportId: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.documents.mapNotNull { it.toAuditEvent() }.sortedByDescending { it.createdAt })
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getExperiences(reportId: String): Flow<List<ReportExperience>> = callbackFlow {
        val subscription = db.collection("experiences")
            .whereEqualTo("reportId", reportId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreReportRepo", "Error fetching experiences for report $reportId: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.documents.mapNotNull { it.toReportExperience() }.sortedByDescending { it.createdAt })
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun shareReport(reportId: String, userId: String): Result<Unit> = runCatching {
        val reportRef = db.collection("reports").document(reportId)
        val userRef = db.collection("users").document(userId)
        
        db.runTransaction { transaction ->
            val reportSnapshot = transaction.get(reportRef)
            val userSnapshot = transaction.get(userRef)
            
            val report = reportSnapshot.toReport() ?: throw Exception("Report not found")
            val user = userSnapshot.toUser() ?: throw Exception("User not found")

            // 1. Increment share count
            val currentShareCount = reportSnapshot.getLong("shareCount") ?: 0
            transaction.update(reportRef, "shareCount", currentShareCount + 1)

            // 2. Create a Community Post for the share
            val postRef = db.collection("posts").document()
            val sharePost = CommunityPost(
                id = postRef.id,
                communityId = report.communityId ?: "global",
                authorId = userId,
                authorName = user.username,
                communityName = report.communityName ?: "Public Feed",
                reportId = reportId,
                title = "Shared: ${report.title}",
                body = "I'm sharing this report to increase awareness in our community.",
                category = report.category,
                urgency = report.urgency,
                status = PostStatus.ACTIVE,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
            transaction.set(postRef, sharePost.toFirestoreMap())
        }.await()
    }
}
