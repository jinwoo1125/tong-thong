package com.example.noisecanseling

private val adjectives = listOf(
    "새벽의", "푸른", "어두운", "빛나는", "차가운", "따뜻한", "깊은", "높은",
    "조용한", "거친", "부드러운", "몽환적인", "투명한", "붉은", "황금빛", "은빛",
    "잔잔한", "강렬한", "멀리서", "가까운", "흐릿한", "선명한", "낯선", "익숙한"
)

private val nouns = listOf(
    "파동", "잔향", "신호", "음파", "파장", "울림", "메아리", "소나기",
    "선율", "화음", "리듬", "비트", "멜로디", "노이즈", "주파수", "진동",
    "바람", "물결", "별빛", "달빛", "안개", "구름", "번개", "천둥"
)

private val prefixes = listOf("A", "B", "C", "D", "E", "F", "G", "H", "N", "M", "R", "S", "T", "Z")
private val codeWords = listOf("트랙", "사운드", "노이즈", "비트", "웨이브", "시그널", "코드", "링크")

// 곡 ID로 항상 같은 블라인드 제목 생성
fun blindTitle(songId: Int): String {
    val adj = adjectives[songId % adjectives.size]
    val noun = nouns[(songId * 7 + 3) % nouns.size]
    return "$adj $noun"
}

// 곡 ID로 항상 같은 아티스트 코드 생성
fun blindArtist(songId: Int): String {
    val prefix = prefixes[songId % prefixes.size]
    val word = codeWords[(songId * 3 + 1) % codeWords.size]
    val num = 10000 + (songId * 9371 + 12345) % 90000
    return "$prefix#$word $num"
}
