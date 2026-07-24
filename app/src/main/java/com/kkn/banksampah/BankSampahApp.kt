package com.kkn.banksampah

import android.app.Application
import com.google.firebase.FirebaseApp

class BankSampahApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
