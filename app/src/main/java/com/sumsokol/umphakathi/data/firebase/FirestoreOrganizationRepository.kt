package com.sumsokol.umphakathi.data.firebase

import com.sumsokol.umphakathi.domain.model.Organization
import com.sumsokol.umphakathi.domain.repository.OrganizationRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreOrganizationRepository(private val db: FirebaseFirestore) : OrganizationRepository {

    override fun getOrganizations(): Flow<List<Organization>> = callbackFlow {
        val subscription = db.collection("organizations")
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreOrgRepo", "Error fetching organizations: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.documents.mapNotNull { it.toOrganization() })
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getOrganization(id: String): Flow<Organization?> = callbackFlow {
        val subscription = db.collection("organizations").document(id)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toOrganization())
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun createOrganization(organization: Organization): Result<Organization> = runCatching {
        val docRef = db.collection("organizations").document()
        val orgWithId = organization.copy(id = docRef.id)
        docRef.set(orgWithId.toFirestoreMap()).await()
        orgWithId
    }
}
