package com.example.noisecanseling.network

data class LoginRequest(val email: String, val password: String)

data class RegisterRequest(val email: String, val password: String, val genres: List<Int> = emptyList())

data class AuthResponse(val token: String, val nickname: String)

data class SongResponse(
    val id: Int,
    val title: String,
    val file_path: String,
    val cover_path: String?,
    val play_count: Int,
    val created_at: String,
    val uploader: String,
    val genre_id: Int?,
    val genre: String?,
    val like_count: Int,
    val comment_count: Int
)

data class CommentResponse(
    val id: Int,
    val content: String,
    val created_at: String,
    val nickname: String
)

data class AddCommentRequest(val content: String)

data class LikeResponse(val liked: Boolean)

data class SaveRequest(val custom_title: String)

data class SaveResponse(
    val id: Int,
    val custom_title: String,
    val created_at: String,
    val song_id: Int,
    val original_title: String,
    val cover_path: String?,
    val uploader: String,
    val genre: String?
)

data class MessageResponse(val message: String)

data class RatingRequest(val score: Int)

data class RatingResponse(
    val avg_score: String?,
    val count: Int,
    val my_score: Int?
)
