package com.example.noisecanseling

import androidx.compose.runtime.mutableStateListOf

data class UserPlaylist(
    val name: String,
    val songs: MutableList<Song> = mutableListOf()
)

// 앱 전체에서 공유하는 플레이리스트 상태
object PlaylistManager {
    val playlists = mutableStateListOf(
        UserPlaylist("내 플레이리스트", songList.toMutableList())
    )

    fun addPlaylist(name: String) {
        if (name.isNotBlank() && playlists.none { it.name == name }) {
            playlists.add(UserPlaylist(name))
        }
    }

    fun addSongToPlaylist(playlistName: String, song: Song) {
        val playlist = playlists.find { it.name == playlistName } ?: return
        if (playlist.songs.none { it.title == song.title }) {
            playlist.songs.add(song)
        }
    }
}
