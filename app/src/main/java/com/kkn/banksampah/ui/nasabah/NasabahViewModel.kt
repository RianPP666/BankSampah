package com.kkn.banksampah.ui.nasabah

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkn.banksampah.data.model.Nasabah
import com.kkn.banksampah.util.UiState
import com.kkn.banksampah.data.repository.NasabahRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NasabahViewModel : ViewModel() {
    private val repository = NasabahRepository()

    private val _allNasabah = MutableStateFlow<List<Nasabah>>(emptyList())
    val allNasabahList: StateFlow<List<Nasabah>> = _allNasabah.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterCategory = MutableStateFlow("Semua") // Semua, Ada Saldo, Saldo Kosong
    val filterCategory: StateFlow<String> = _filterCategory.asStateFlow()
    
    val nasabahList: StateFlow<List<Nasabah>> = combine(_allNasabah, _searchQuery, _filterCategory) { list, query, category ->
        var filtered = if (query.isBlank()) {
            list
        } else {
            val q = query.lowercase().trim()
            list.filter { 
                it.nama.lowercase().contains(q) || 
                it.noHp.lowercase().contains(q) || 
                it.alamat.lowercase().contains(q) 
            }
        }

        when (category) {
            "Ada Saldo" -> filtered.filter { it.saldo > 0 }
            "Saldo Kosong" -> filtered.filter { it.saldo <= 0 }
            else -> filtered
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _operationState = MutableStateFlow<UiState<Unit>>(UiState.Success(Unit))
    val operationState: StateFlow<UiState<Unit>> = _operationState.asStateFlow()

    init {
        loadNasabah()
    }

    fun loadNasabah() {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            repository.getAll().catch { e ->
                _operationState.value = UiState.Error(e.message ?: "Gagal memuat data nasabah")
            }.collect { list ->
                _allNasabah.value = list
                _operationState.value = UiState.Success(Unit)
            }
        }
    }

    fun addNasabah(nama: String, alamat: String, noHp: String) {
        val cleanNama = nama.trim()
        val cleanAlamat = alamat.trim()
        val cleanHp = noHp.trim()

        if (cleanNama.isBlank()) {
            _operationState.value = UiState.Error("Nama nasabah tidak boleh kosong")
            return
        }
        if (cleanAlamat.isBlank()) {
            _operationState.value = UiState.Error("Alamat tidak boleh kosong")
            return
        }
        if (cleanHp.isBlank() || !cleanHp.matches(Regex("^[0-9]{10,14}$"))) {
            _operationState.value = UiState.Error("Nomor HP harus berupa 10-14 digit angka")
            return
        }

        // Cek Duplikat
        val isDuplicateHp = _allNasabah.value.any { it.noHp.trim() == cleanHp }
        if (isDuplicateHp) {
            _operationState.value = UiState.Error("Nomor HP '$cleanHp' sudah terdaftar pada nasabah lain")
            return
        }

        viewModelScope.launch {
            _operationState.value = UiState.Loading
            try {
                repository.add(Nasabah(id = "", nama = cleanNama, alamat = cleanAlamat, noHp = cleanHp, saldo = 0.0))
                _operationState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _operationState.value = UiState.Error(e.message ?: "Gagal menambah nasabah")
            }
        }
    }

    fun updateNasabah(nasabah: Nasabah) {
        val cleanNama = nasabah.nama.trim()
        val cleanAlamat = nasabah.alamat.trim()
        val cleanHp = nasabah.noHp.trim()

        if (cleanNama.isBlank()) {
            _operationState.value = UiState.Error("Nama nasabah tidak boleh kosong")
            return
        }
        if (cleanAlamat.isBlank()) {
            _operationState.value = UiState.Error("Alamat tidak boleh kosong")
            return
        }
        if (cleanHp.isBlank() || !cleanHp.matches(Regex("^[0-9]{10,14}$"))) {
            _operationState.value = UiState.Error("Nomor HP harus berupa 10-14 digit angka")
            return
        }

        // Cek Duplikat No HP selain milik nasabah ini sendiri
        val isDuplicateHp = _allNasabah.value.any { it.id != nasabah.id && it.noHp.trim() == cleanHp }
        if (isDuplicateHp) {
            _operationState.value = UiState.Error("Nomor HP '$cleanHp' sudah terdaftar pada nasabah lain")
            return
        }

        viewModelScope.launch {
            _operationState.value = UiState.Loading
            try {
                repository.update(nasabah.copy(nama = cleanNama, alamat = cleanAlamat, noHp = cleanHp))
                _operationState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _operationState.value = UiState.Error(e.message ?: "Gagal mengubah nasabah")
            }
        }
    }

    fun deleteNasabah(id: String) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            try {
                repository.delete(id)
                _operationState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _operationState.value = UiState.Error(e.message ?: "Gagal menghapus nasabah")
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterCategory(category: String) {
        _filterCategory.value = category
    }

    fun resetState() {
        _operationState.value = UiState.Success(Unit)
    }
}

