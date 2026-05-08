package com.example.noisecanseling

import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf

object AppPlayer {
    val currentSongId = mutableIntStateOf(-1)
    val currentTitle = mutableStateOf("블라인드 트랙")
    val currentArtist = mutableStateOf("")
    val isPlaying = mutableStateOf(false)
    val sliderPosition = mutableFloatStateOf(0f)
    val duration = mutableLongStateOf(0L)
}
