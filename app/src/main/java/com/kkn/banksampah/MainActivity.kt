package com.kkn.banksampah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.auth.FirebaseAuth
import com.kkn.banksampah.navigation.AppNavigation
import com.kkn.banksampah.navigation.Screen
import com.kkn.banksampah.ui.theme.BankSampahTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val startDest = if (FirebaseAuth.getInstance().currentUser != null) {
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
