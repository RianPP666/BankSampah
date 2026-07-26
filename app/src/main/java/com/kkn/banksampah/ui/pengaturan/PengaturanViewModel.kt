package com.kkn.banksampah.ui.pengaturan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkn.banksampah.data.model.User
import com.kkn.banksampah.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PengaturanViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    init {
        loadUser()
    }

    fun loadUser() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            _user.value = User(
                uid = currentUser?.uid ?: "",
                nama = currentUser?.nama ?: "Petugas",
                email = currentUser?.email ?: "email@example.com",
                role = "Petugas"
            )
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun resetDatabase(onComplete: (Boolean, String) -> Unit) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        viewModelScope.launch {
            try {
                val collections = listOf("nasabah", "transaksi", "penjualan", "jenis_sampah")
                for (collectionName in collections) {
                    val snapshot = db.collection(collectionName).get().await()
                    for (doc in snapshot.documents) {
                        doc.reference.delete().await()
                    }
                }
                onComplete(true, "Database berhasil dikosongkan. Silakan mulai dari awal!")
            } catch (e: Exception) {
                onComplete(false, "Gagal mengosongkan database: ${e.message}")
            }
        }
    }
}
