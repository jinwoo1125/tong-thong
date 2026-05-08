package com.example.noisecanseling

import androidx.compose.runtime.mutableStateSetOf

object LikedSongsManager {
    val likedSongIds = mutableStateSetOf<Int>()

    fun toggle(songId: Int) {
        if (songId in likedSongIds) likedSongIds.remove(songId) else likedSongIds.add(songId)
    }

    fun isLiked(songId: Int) = songId in likedSongIds
}
