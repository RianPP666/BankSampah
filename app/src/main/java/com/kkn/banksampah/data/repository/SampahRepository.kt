package com.kkn.banksampah.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kkn.banksampah.data.model.JenisSampah
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SampahRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val jenisSampahCollection = firestore.collection("jenisSampah")

    fun getAll(): Flow<List<JenisSampah>> = callbackFlow {
        val listener = jenisSampahCollection
            .orderBy("nama", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(JenisSampah::class.java)?.copy(id = it.id) }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun add(jenisSampah: JenisSampah): Result<String> {
        return try {
            val docRef = jenisSampahCollection.document()
            val dataWithId = jenisSampah.copy(id = docRef.id)
            docRef.set(dataWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun update(jenisSampah: JenisSampah): Result<Unit> {
        return try {
            if (jenisSampah.id.isEmpty()) throw Exception("ID is empty")
            jenisSampahCollection.document(jenisSampah.id).set(jenisSampah).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun delete(id: String): Result<Unit> {
        return try {
            jenisSampahCollection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
