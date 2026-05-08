package com.example.noisecanseling

import android.app.Application
import com.example.noisecanseling.network.TokenManager

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenManager.init(this)
    }
}
