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
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    val nasabahList: StateFlow<List<Nasabah>> = combine(_allNasabah, _searchQuery) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            list.filter { it.nama.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _operationState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val operationState: StateFlow<UiState<Unit>> = _operationState.asStateFlow()

    init {
        loadNasabah()
    }

    fun loadNasabah() {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            repository.getAll().catch { e ->
                _operationState.value = UiState.Error(e.message ?: "Unknown Error")
            }.collect { list ->
                _allNasabah.value = list
                _operationState.value = UiState.Success(Unit)
            }
        }
    }

    fun addNasabah(nama: String, alamat: String, noHp: String) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            try {
                repository.add(Nasabah(id = "", nama = nama, alamat = alamat, noHp = noHp, saldo = 0.0))
                _operationState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _operationState.value = UiState.Error(e.message ?: "Gagal menambah nasabah")
            }
        }
    }

    fun updateNasabah(nasabah: Nasabah) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            try {
                repository.update(nasabah)
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
}
