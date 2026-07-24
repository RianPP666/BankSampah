package com.kkn.banksampah.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkn.banksampah.data.model.User
import com.kkn.banksampah.data.repository.AuthRepository
import com.kkn.banksampah.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _loginState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val loginState: StateFlow<UiState<User>> = _loginState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            if (username.isBlank() || password.isBlank()) {
                _loginState.value = UiState.Error("Username dan password tidak boleh kosong")
                return@launch
            }
            _loginState.value = UiState.Loading
            val result = authRepository.login(username, password)
            if (result.isSuccess) {
                _loginState.value = UiState.Success(result.getOrNull()!!)
            } else {
                _loginState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Login gagal")
            }
        }
    }

    fun checkLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }
}
