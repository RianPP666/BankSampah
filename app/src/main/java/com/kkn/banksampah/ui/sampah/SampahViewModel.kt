package com.kkn.banksampah.ui.sampah

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkn.banksampah.data.model.JenisSampah
import com.kkn.banksampah.util.UiState
import com.kkn.banksampah.data.repository.SampahRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SampahViewModel : ViewModel() {
    private val repository = SampahRepository()

    private val _jenisSampahList = MutableStateFlow<List<JenisSampah>>(emptyList())
    val jenisSampahList: StateFlow<List<JenisSampah>> = _jenisSampahList.asStateFlow()

    private val _operationState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val operationState: StateFlow<UiState<Unit>> = _operationState.asStateFlow()

    init {
        loadJenisSampah()
    }

    fun loadJenisSampah() {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            repository.getAll().catch { e ->
                _operationState.value = UiState.Error(e.message ?: "Unknown Error")
            }.collect { list ->
                _jenisSampahList.value = list
                _operationState.value = UiState.Success(Unit)
            }
        }
    }

    fun addJenisSampah(nama: String, kategori: String, hargaPerSatuan: Double, deskripsi: String) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            try {
                repository.add(
                    JenisSampah(id = "", nama = nama, kategori = kategori, hargaPerSatuan = hargaPerSatuan, deskripsi = deskripsi)
                )
                _operationState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _operationState.value = UiState.Error(e.message ?: "Gagal menambah jenis sampah")
            }
        }
    }

    fun updateJenisSampah(jenisSampah: JenisSampah) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            try {
                repository.update(jenisSampah)
                _operationState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _operationState.value = UiState.Error(e.message ?: "Gagal mengubah jenis sampah")
            }
        }
    }

    fun deleteJenisSampah(id: String) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            try {
                repository.delete(id)
                _operationState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _operationState.value = UiState.Error(e.message ?: "Gagal menghapus jenis sampah")
            }
        }
    }
}
