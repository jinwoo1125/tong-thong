package com.example.noisecanseling

import android.app.Application
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import com.example.noisecanseling.network.TokenManager

class App : Application() {
    val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                /* handleAudioFocus= */ true
            )
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        TokenManager.init(this)
    }
}
