package com.kkn.banksampah.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.kkn.banksampah.data.model.Nasabah
import com.kkn.banksampah.data.model.Transaksi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar

data class DashboardStats(
    val totalNasabah: Int = 0,
    val totalSampahKg: Double = 0.0,
    val totalSaldo: Double = 0.0,
    val transaksiHariIni: Int = 0
)

data class LaporanBulanan(
    val bulan: Int = 0,
    val tahun: Int = 0,
    val totalSetor: Double = 0.0,
    val totalTarik: Double = 0.0,
    val totalKg: Double = 0.0,
    val jumlahTransaksi: Int = 0,
    val topNasabah: List<NasabahStat> = emptyList(),
    val daftarPenyetor: List<NasabahStat> = emptyList()
)

data class NasabahStat(
    val idNasabah: String = "",
    val nama: String = "",
    val totalSetor: Double = 0.0,
    val totalKg: Double = 0.0,
    val jumlahTransaksi: Int = 0
)

class LaporanRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val nasabahCollection = firestore.collection("nasabah")
    private val transaksiCollection = firestore.collection("transaksi")

    fun getDashboardStats(): Flow<DashboardStats> = callbackFlow {
        // Since we are combining multiple collections, doing it via a callback flow might be complex.
        // A simple approach is to listen to nasabah and transaksi, but for stats, we might just poll or fetch.
        // For real-time dashboard:
        val nasabahListener = nasabahCollection.addSnapshotListener { nasabahSnapshot, nasabahError ->
            if (nasabahError != null) return@addSnapshotListener
            
            transaksiCollection.addSnapshotListener { transSnapshot, transError ->
                if (transError != null) return@addSnapshotListener
                
                if (nasabahSnapshot != null && transSnapshot != null) {
                    val nasabahList = nasabahSnapshot.documents.mapNotNull { it.toObject(Nasabah::class.java) }
                    val transList = transSnapshot.documents.mapNotNull { it.toObject(Transaksi::class.java) }

                    val totalNasabah = nasabahList.size
                    val totalSaldo = nasabahList.sumOf { it.saldo }
                    
                    val totalSampahKg = transList.filter { it.jenisTransaksi == "SETOR" }
                        .flatMap { it.detailSampah }
                        .sumOf { it.beratKg }
                        
                    // Transaksi Hari ini
                    val today = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    
                    val transHariIni = transList.count { it.tanggal >= today }
                    
                    trySend(DashboardStats(totalNasabah, totalSampahKg, totalSaldo, transHariIni))
                }
            }
        }
        
        awaitClose { 
            // In a real app we'd manage the multiple listeners properly
            nasabahListener.remove() 
        }
    }

    suspend fun getLaporanBulanan(bulan: Int, tahun: Int): LaporanBulanan {
        // bulan is 0-indexed (0 = Jan) if using Calendar, adjust as needed.
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, tahun)
        calendar.set(Calendar.MONTH, bulan)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfMonth = calendar.timeInMillis
        
        calendar.add(Calendar.MONTH, 1)
        val endOfMonth = calendar.timeInMillis
        
        val transSnapshot = transaksiCollection
            .whereGreaterThanOrEqualTo("tanggal", startOfMonth)
            .whereLessThan("tanggal", endOfMonth)
            .get()
            .await()
            
        val transList = transSnapshot.documents.mapNotNull { it.toObject(Transaksi::class.java) }
        
        val totalSetor = transList.filter { it.jenisTransaksi == "SETOR" }.sumOf { it.totalRupiah }
        val totalTarik = transList.filter { it.jenisTransaksi == "TARIK" }.sumOf { it.totalRupiah }
        val totalKg = transList.filter { it.jenisTransaksi == "SETOR" }
            .flatMap { it.detailSampah }
            .sumOf { it.beratKg }
            
        val topNasabahMap = mutableMapOf<String, NasabahStat>()
        
        transList.filter { it.jenisTransaksi == "SETOR" }.forEach { trans ->
            val kgTrans = trans.detailSampah.sumOf { it.beratKg }
            val current = topNasabahMap[trans.idNasabah]
            if (current == null) {
                topNasabahMap[trans.idNasabah] = NasabahStat(
                    idNasabah = trans.idNasabah,
                    nama = trans.namaNasabah,
                    totalSetor = trans.totalRupiah,
                    totalKg = kgTrans,
                    jumlahTransaksi = 1
                )
            } else {
                topNasabahMap[trans.idNasabah] = current.copy(
                    totalSetor = current.totalSetor + trans.totalRupiah,
                    totalKg = current.totalKg + kgTrans,
                    jumlahTransaksi = current.jumlahTransaksi + 1
                )
            }
        }
        
        val sortedPenyetor = topNasabahMap.values.sortedByDescending { it.totalSetor }
        val topNasabah = sortedPenyetor.take(5)
        val transCount = transList.size
        
        return LaporanBulanan(bulan, tahun, totalSetor, totalTarik, totalKg, transCount, topNasabah, sortedPenyetor)
    }
}
