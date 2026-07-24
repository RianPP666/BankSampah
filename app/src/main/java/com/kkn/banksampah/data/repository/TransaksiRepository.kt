package com.kkn.banksampah.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kkn.banksampah.data.model.Nasabah
import com.kkn.banksampah.data.model.Transaksi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TransaksiRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val transaksiCollection = firestore.collection("transaksi")
    private val nasabahCollection = firestore.collection("nasabah")

    suspend fun setor(transaksi: Transaksi): Result<Unit> {
        return try {
            val docRef = transaksiCollection.document()
            val transWithId = transaksi.copy(id = docRef.id, jenisTransaksi = "SETOR")
            
            val nasabahRef = nasabahCollection.document(transaksi.idNasabah)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(nasabahRef)
                val nasabah = snapshot.toObject(Nasabah::class.java)
                    ?: throw Exception("Nasabah tidak ditemukan")

                val newSaldo = nasabah.saldo + transaksi.totalRupiah
                
                transaction.update(nasabahRef, "saldo", newSaldo)
                transaction.set(docRef, transWithId)
                null
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun tarik(idNasabah: String, amount: Double, petugasId: String, petugasNama: String): Result<Unit> {
        return try {
            val docRef = transaksiCollection.document()
            val nasabahRef = nasabahCollection.document(idNasabah)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(nasabahRef)
                val nasabah = snapshot.toObject(Nasabah::class.java)
                    ?: throw Exception("Nasabah tidak ditemukan")

                if (nasabah.saldo < amount) {
                    throw Exception("Saldo tidak mencukupi")
                }

                val newSaldo = nasabah.saldo - amount
                
                val transaksi = Transaksi(
                    id = docRef.id,
                    idNasabah = nasabah.id,
                    namaNasabah = nasabah.nama,
                    jenisTransaksi = "TARIK",
                    totalRupiah = amount,
                    petugasId = petugasId,
                    petugasNama = petugasNama
                )

                transaction.update(nasabahRef, "saldo", newSaldo)
                transaction.set(docRef, transaksi)
                null
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAll(): Flow<List<Transaksi>> = callbackFlow {
        val listener = transaksiCollection
            .orderBy("tanggal", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(Transaksi::class.java)?.copy(id = it.id) }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    fun getRecentTransactions(limit: Int): Flow<List<Transaksi>> = callbackFlow {
        val listener = transaksiCollection
            .orderBy("tanggal", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(Transaksi::class.java)?.copy(id = it.id) }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    fun getByNasabah(idNasabah: String): Flow<List<Transaksi>> = callbackFlow {
        val listener = transaksiCollection
            .whereEqualTo("idNasabah", idNasabah)
            .orderBy("tanggal", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(Transaksi::class.java)?.copy(id = it.id) }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }
}
