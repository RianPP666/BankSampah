package com.kkn.banksampah.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kkn.banksampah.data.model.Penjualan
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PenjualanRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val penjualanCollection = firestore.collection("penjualan")

    suspend fun add(penjualan: Penjualan): Result<String> {
        return try {
            val docRef = penjualanCollection.document()
            val dataWithId = penjualan.copy(id = docRef.id)
            docRef.set(dataWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAll(): Flow<List<Penjualan>> = callbackFlow {
        val listener = penjualanCollection
            .orderBy("tanggal", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull {
                        it.toObject(Penjualan::class.java)?.copy(id = it.id)
                    }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun getTotalPenjualan(): Double {
        return try {
            val snapshot = penjualanCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject(Penjualan::class.java) }
                .sumOf { it.totalHargaJual }
        } catch (e: Exception) {
            0.0
        }
    }

    suspend fun getTotalBeratDijual(): Double {
        return try {
            val snapshot = penjualanCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject(Penjualan::class.java) }
                .sumOf { it.totalBeratKg }
        } catch (e: Exception) {
            0.0
        }
    }
}
