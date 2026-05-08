package com.example.noisecanseling

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

data class UserPlaylist(
    val name: String,
    val songs: SnapshotStateList<Song> = mutableStateListOf()
)

// 앱 전체에서 공유하는 플레이리스트 상태
object PlaylistManager {
    val playlists = mutableStateListOf(
        UserPlaylist("내 플레이리스트", mutableStateListOf(*songList.toTypedArray()))
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

    // 홈 테마 저장 — 동일 이름 플레이리스트가 없으면 생성 후 곡 추가
    fun syncThemePlaylist(playlist: MoodPlaylist) {
        val existing = playlists.find { it.name == playlist.title }
        if (existing == null) {
            val songs = mutableStateListOf(*playlist.songs.map { Song(it.title, it.genre, it.totalStreams.replace(",","").toIntOrNull() ?: 0, emptyList()) }.toTypedArray())
            playlists.add(UserPlaylist(playlist.title, songs))
        } else {
            playlist.songs.forEach { chartSong ->
                val song = Song(chartSong.title, chartSong.genre, 0, emptyList())
                if (existing.songs.none { it.title == chartSong.title }) existing.songs.add(song)
            }
        }
    }

    fun hasThemePlaylist(name: String) = playlists.any { it.name == name }
}
