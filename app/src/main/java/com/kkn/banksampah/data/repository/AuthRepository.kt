package com.kkn.banksampah.data.repository

import com.kkn.banksampah.data.model.User

class AuthRepository {

    companion object {
        private var currentUser: User? = null
    }

    suspend fun login(username: String, password: String): Result<User> {
        return if (username == "admin" && password == "admin") {
            val user = User(uid = "admin-123", email = "admin@banksampah.com", nama = "Admin", role = "admin")
            currentUser = user
            Result.success(user)
        } else {
            Result.failure(Exception("Username atau password salah"))
        }
    }

    fun logout() {
        currentUser = null
    }

    fun getCurrentUser(): User? {
        return currentUser
    }

    suspend fun getUserRole(uid: String): String {
        return "admin"
    }

    fun isLoggedIn(): Boolean {
        return currentUser != null
    }
}
