package com.example.noisecanseling

import androidx.compose.runtime.mutableStateMapOf

// 곡별 스트리밍 카운트 (song.id % songList.size → 추가 재생 횟수)
val songStreamCounts = mutableStateMapOf<Int, Int>()

// 곡 재생 시 호출 — 30초 이상 재생됐을 때 1회 카운트
fun recordStream(songIndex: Int) {
    val prev = songStreamCounts[songIndex] ?: 0
    songStreamCounts[songIndex] = prev + 1
}
