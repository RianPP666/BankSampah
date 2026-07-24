package com.kkn.banksampah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.kkn.banksampah.data.repository.AuthRepository
import com.kkn.banksampah.navigation.AppNavigation
import com.kkn.banksampah.navigation.Screen
import com.kkn.banksampah.ui.theme.BankSampahTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Enable Firestore Offline Persistence for instant data loading
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
            FirebaseFirestore.getInstance().firestoreSettings = settings
        } catch (e: Exception) {
            // Settings already set or ignored
        }

        val authRepository = AuthRepository()
        val startDest = if (authRepository.isLoggedIn()) {
            Screen.Dashboard.route
        } else {
            Screen.Login.route
        }

        setContent {
            BankSampahTheme {
                AppNavigation(startDestination = startDest)
            }
        }
    }
}
