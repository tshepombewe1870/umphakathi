package com.sumsokol.umphakathi.data.firebase

import com.sumsokol.umphakathi.domain.model.VolunteerOffer
import com.sumsokol.umphakathi.domain.model.VolunteerStatus
import com.sumsokol.umphakathi.domain.model.ResourceType
import com.sumsokol.umphakathi.domain.repository.VolunteerRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant

class FirestoreVolunteerRepository(private val db: FirebaseFirestore) : VolunteerRepository {

    override fun getOffersForReport(reportId: String): Flow<List<VolunteerOffer>> = callbackFlow {
        val subscription = db.collection("volunteerOffers")
            .whereEqualTo("reportId", reportId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreVolunteerRepo", "Error fetching offers for report $reportId: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.documents.mapNotNull { it.toVolunteerOffer() }.sortedByDescending { it.createdAt })
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getOffersForCrisis(crisisId: String): Flow<List<VolunteerOffer>> = callbackFlow {
        val subscription = db.collection("volunteerOffers")
            .whereEqualTo("crisisId", crisisId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreVolunteerRepo", "Error fetching offers for crisis $crisisId: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.documents.mapNotNull { it.toVolunteerOffer() }.sortedByDescending { it.createdAt })
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getOffersByUser(userId: String): Flow<List<VolunteerOffer>> = callbackFlow {
        val subscription = db.collection("volunteerOffers")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.documents.mapNotNull { it.toVolunteerOffer() })
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun submitOffer(offer: VolunteerOffer): Result<VolunteerOffer> = runCatching {
        val docRef = db.collection("volunteerOffers").document()
        val offerWithId = offer.copy(id = docRef.id)
        
        db.runTransaction { transaction ->
            // 1. Set the offer
            transaction.set(docRef, offerWithId.toFirestoreMap())
            
            // 2. Increment volunteerCount on report or crisis
            if (!offer.reportId.isNullOrBlank()) {
                val reportRef = db.collection("reports").document(offer.reportId)
                transaction.update(reportRef, "volunteerCount", com.google.firebase.firestore.FieldValue.increment(1))
            }
            if (!offer.crisisId.isNullOrBlank()) {
                val crisisRef = db.collection("crises").document(offer.crisisId)
                transaction.update(crisisRef, "volunteerCount", com.google.firebase.firestore.FieldValue.increment(1))
            }
        }.await()
        
        offerWithId
    }

    override suspend fun updateOfferStatus(offerId: String, status: VolunteerStatus): Result<Unit> = runCatching {
        db.collection("volunteerOffers").document(offerId)
            .update("status", status.name).await()
    }

    override suspend fun toggleLikeOffer(offerId: String, userId: String): Result<Unit> = runCatching {
        val likeRef = db.collection("volunteerOfferLikes").document("${offerId}_${userId}")
        val offerRef = db.collection("volunteerOffers").document(offerId)
        
        db.runTransaction { transaction ->
            val likeSnapshot = transaction.get(likeRef)
            val offerSnapshot = transaction.get(offerRef)
            val currentLikes = offerSnapshot.getLong("likeCount") ?: 0
            
            if (likeSnapshot.exists()) {
                transaction.delete(likeRef)
                transaction.update(offerRef, "likeCount", (currentLikes - 1).coerceAtLeast(0))
            } else {
                transaction.set(likeRef, mapOf("offerId" to offerId, "userId" to userId, "createdAt" to com.google.firebase.Timestamp.now()))
                transaction.update(offerRef, "likeCount", currentLikes + 1)
            }
        }.await()
    }

    override fun getComments(offerId: String): Flow<List<com.sumsokol.umphakathi.domain.model.Comment>> = callbackFlow {
        val subscription = db.collection("volunteerComments")
            .whereEqualTo("offerId", offerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.documents.mapNotNull { it.toComment() }.sortedBy { it.createdAt })
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun addComment(comment: com.sumsokol.umphakathi.domain.model.Comment): Result<com.sumsokol.umphakathi.domain.model.Comment> = runCatching {
        val docRef = db.collection("volunteerComments").document()
        val commentWithId = comment.copy(id = docRef.id)
        
        db.runTransaction { transaction ->
            transaction.set(docRef, commentWithId.toFirestoreMap().toMutableMap().apply { 
                put("offerId", comment.postId) // Reusing postId as offerId
            })
            
            db.collection("volunteerOffers").document(comment.postId)
                .update("commentCount", com.google.firebase.firestore.FieldValue.increment(1))
        }.await()
        
        commentWithId
    }
}
