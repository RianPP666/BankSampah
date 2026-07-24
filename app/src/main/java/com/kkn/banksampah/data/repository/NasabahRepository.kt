package com.kkn.banksampah.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kkn.banksampah.data.model.Nasabah
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NasabahRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val nasabahCollection = firestore.collection("nasabah")

    fun getAll(): Flow<List<Nasabah>> = callbackFlow {
        val listener = nasabahCollection
            .orderBy("nama", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(Nasabah::class.java)?.copy(id = it.id) }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun getById(id: String): Nasabah? {
        return try {
            val snapshot = nasabahCollection.document(id).get().await()
            snapshot.toObject(Nasabah::class.java)?.copy(id = snapshot.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun add(nasabah: Nasabah): Result<String> {
        return try {
            val docRef = nasabahCollection.document()
            val nasabahWithId = nasabah.copy(id = docRef.id)
            docRef.set(nasabahWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun update(nasabah: Nasabah): Result<Unit> {
        return try {
            if (nasabah.id.isEmpty()) throw Exception("Nasabah ID is empty")
            nasabahCollection.document(nasabah.id).set(nasabah).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun delete(id: String): Result<Unit> {
        return try {
            nasabahCollection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun search(query: String): Flow<List<Nasabah>> = callbackFlow {
        // Firestore doesn't have a simple full-text search or 'LIKE' equivalent easily without extensions,
        // but for a small dataset, we can fetch all or do a prefix search.
        // Doing a simple client-side filter after fetching ordered data, or prefix query:
        val listener = nasabahCollection
            .orderBy("nama")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(Nasabah::class.java)?.copy(id = it.id) }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }
}
