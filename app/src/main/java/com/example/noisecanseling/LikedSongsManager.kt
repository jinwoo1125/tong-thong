package com.example.noisecanseling

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateSetOf
import com.example.noisecanseling.network.SongResponse

object LikedSongsManager {
    val likedSongIds = mutableStateSetOf<Int>()
    private val songCache = mutableStateMapOf<Int, SongResponse>()

    fun like(song: SongResponse) {
        likedSongIds.add(song.id)
        songCache[song.id] = song
    }

    fun unlike(songId: Int) {
        likedSongIds.remove(songId)
        songCache.remove(songId)
    }

    fun toggle(song: SongResponse) {
        if (song.id in likedSongIds) unlike(song.id) else like(song)
    }

    fun isLiked(songId: Int) = songId in likedSongIds

    fun getLikedSongs(): List<SongResponse> = likedSongIds.mapNotNull { songCache[it] }
}
