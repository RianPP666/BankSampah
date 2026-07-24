package com.kkn.banksampah.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kkn.banksampah.data.model.User
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("Login failed: UID is null")
            
            val docSnapshot = usersCollection.document(uid).get().await()
            val user = docSnapshot.toObject(User::class.java)
                ?: throw Exception("User data not found in Firestore")
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        // Note: this returns a basic user object. To get role, you might need a separate call
        // if it's not cached, but typically you fetch the full User object when needed.
        return User(uid = firebaseUser.uid, email = firebaseUser.email ?: "")
    }

    suspend fun getUserRole(uid: String): String {
        return try {
            val doc = usersCollection.document(uid).get().await()
            doc.getString("role") ?: "petugas"
        } catch (e: Exception) {
            "petugas"
        }
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}
