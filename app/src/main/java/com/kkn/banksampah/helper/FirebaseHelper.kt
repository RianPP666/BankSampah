package com.kkn.banksampah.helper

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kkn.banksampah.model.*

object FirebaseHelper {

    private val db = FirebaseFirestore.getInstance()

    // ==================== NASABAH ====================

    fun tambahNasabah(nasabah: Nasabah, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("nasabah")
            .add(nasabah)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun updateNasabah(nasabah: Nasabah, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("nasabah").document(nasabah.id ?: "")
            .set(nasabah)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun hapusNasabah(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("nasabah").document(id)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getAllNasabah(onSuccess: (List<Nasabah>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("nasabah")
            .orderBy("nama")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFailure(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(Nasabah::class.java) ?: emptyList()
                onSuccess(list)
            }
    }

    fun updateSaldoNasabah(idNasabah: String, jumlah: Double, isSetor: Boolean, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val ref = db.collection("nasabah").document(idNasabah)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            val currentSaldo = snapshot.getDouble("saldo") ?: 0.0
            val newSaldo = if (isSetor) currentSaldo + jumlah else currentSaldo - jumlah
            if (!isSetor && newSaldo < 0) {
                throw Exception("Saldo tidak mencukupi")
            }
            transaction.update(ref, "saldo", newSaldo)
        }
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // ==================== JENIS SAMPAH ====================

    fun tambahJenisSampah(jenisSampah: JenisSampah, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("jenis_sampah")
            .add(jenisSampah)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun hapusJenisSampah(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("jenis_sampah").document(id)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getAllJenisSampah(onSuccess: (List<JenisSampah>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("jenis_sampah")
            .orderBy("namaSampah")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFailure(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(JenisSampah::class.java) ?: emptyList()
                onSuccess(list)
            }
    }

    // ==================== TRANSAKSI ====================

    fun simpanTransaksi(transaksi: Transaksi, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("transaksi")
            .add(transaksi)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getAllTransaksi(onSuccess: (List<Transaksi>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("transaksi")
            .orderBy("tanggal", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFailure(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(Transaksi::class.java) ?: emptyList()
                onSuccess(list)
            }
    }

    // ==================== DASHBOARD STATS ====================

    fun getDashboardStats(
        onResult: (totalNasabah: Int, totalSampahKg: Double, totalSaldo: Double) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Get total nasabah and saldo
        db.collection("nasabah").addSnapshotListener { nasabahSnapshot, error1 ->
            if (error1 != null) {
                onFailure(error1)
                return@addSnapshotListener
            }
            val totalNasabah = nasabahSnapshot?.size() ?: 0
            val totalSaldo = nasabahSnapshot?.documents?.sumOf {
                it.getDouble("saldo") ?: 0.0
            } ?: 0.0

            // Get total sampah from transactions
            db.collection("transaksi")
                .whereEqualTo("jenisTransaksi", "SETOR")
                .addSnapshotListener { transaksiSnapshot, error2 ->
                    if (error2 != null) {
                        onFailure(error2)
                        return@addSnapshotListener
                    }
                    var totalSampahKg = 0.0
                    transaksiSnapshot?.documents?.forEach { doc ->
                        val details = doc.get("detailSampah") as? List<*>
                        details?.forEach { item ->
                            if (item is Map<*, *>) {
                                totalSampahKg += (item["beratKg"] as? Double) ?: 0.0
                            }
                        }
                    }
                    onResult(totalNasabah, totalSampahKg, totalSaldo)
                }
        }
    }
}
