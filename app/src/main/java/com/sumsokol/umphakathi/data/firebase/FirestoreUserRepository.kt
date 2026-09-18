package com.sumsokol.umphakathi.data.firebase

import com.sumsokol.umphakathi.domain.model.User
import com.sumsokol.umphakathi.domain.model.AccountType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant

class FirestoreUserRepository(private val db: FirebaseFirestore) {

    fun getUser(uid: String): Flow<User?> = callbackFlow {
        val subscription = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreUserRepo", "Error fetching user: ${error.message}")
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toUser())
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun getOrCreateUser(
        uid: String,
        username: String? = null,
        role: String? = null,
        communityName: String? = null
    ): User {
        val docRef = db.collection("users").document(uid)
        val snapshot = docRef.get().await()
        
        return if (snapshot.exists()) {
            snapshot.toUser() ?: createDefaultUser(uid, username, role, communityName)
        } else {
            val newUser = createDefaultUser(uid, username, role, communityName)
            docRef.set(newUser.toFirestoreMap()).await()
            newUser
        }
    }

    private fun createDefaultUser(
        uid: String,
        username: String?,
        role: String?,
        communityName: String?
    ): User {
        return User(
            id = uid,
            username = username ?: "user_${uid.take(4)}",
            role = role,
            communityName = communityName,
            accountType = if (role == "Official" || role == "Responder") AccountType.ORGANIZATION else AccountType.PERSON,
            verified = true, // Auto-verify demo personas
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }
}
