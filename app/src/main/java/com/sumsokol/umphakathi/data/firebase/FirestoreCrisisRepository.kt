package com.sumsokol.umphakathi.data.firebase

import com.sumsokol.umphakathi.domain.model.*
import com.sumsokol.umphakathi.domain.repository.CrisisRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant

class FirestoreCrisisRepository(private val db: FirebaseFirestore) : CrisisRepository {

    override fun getCrises(): Flow<List<Crisis>> = callbackFlow {
        val subscription = db.collection("crises")
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreCrisisRepo", "Error fetching crises: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.documents.mapNotNull { it.toCrisis() })
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getCrisis(id: String): Flow<Crisis?> = callbackFlow {
        val subscription = db.collection("crises").document(id)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toCrisis())
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun createCrisis(crisis: Crisis): Result<Crisis> = runCatching {
        val docRef = db.collection("crises").document()
        val crisisWithId = crisis.copy(id = docRef.id)
        docRef.set(crisisWithId.toFirestoreMap()).await()
        crisisWithId
    }

    override suspend fun updateCrisis(crisis: Crisis): Result<Crisis> = runCatching {
        db.collection("crises").document(crisis.id).set(crisis.toFirestoreMap()).await()
        crisis
    }

    override suspend fun addVolunteerOffer(crisisId: String, offer: VolunteerOffer): Result<Unit> = runCatching {
        val offerRef = db.collection("volunteerOffers").document()
        val offerWithId = offer.copy(id = offerRef.id, crisisId = crisisId)
        offerRef.set(offerWithId.toFirestoreMap()).await()
        
        val crisisRef = db.collection("crises").document(crisisId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(crisisRef)
            val currentCount = snapshot.getLong("volunteerCount") ?: 0
            transaction.update(crisisRef, "volunteerCount", currentCount + 1)
        }.await()
    }
}
