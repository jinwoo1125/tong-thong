package com.example.noisecanseling.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── 인증 ──────────────────────────────────────
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    // ── 음악 ──────────────────────────────────────
    @GET("api/songs")
    suspend fun getSongs(
        @Query("genre_id") genreId: Int? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<List<SongResponse>>

    @GET("api/songs/{id}")
    suspend fun getSong(@Path("id") id: Int): Response<SongResponse>

    @Multipart
    @POST("api/songs")
    suspend fun uploadSong(
        @Header("Authorization") token: String,
        @Part song: MultipartBody.Part,
        @Part cover: MultipartBody.Part?,
        @Part("title") title: RequestBody,
        @Part("genre_id") genreId: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("lyrics") lyrics: RequestBody?
    ): Response<MessageResponse>

    // ── 좋아요 ────────────────────────────────────
    @POST("api/songs/{id}/like")
    suspend fun toggleLike(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<LikeResponse>

    @GET("api/songs/{id}/like")
    suspend fun getLikeStatus(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<LikeResponse>

    // ── 댓글 ──────────────────────────────────────
    @GET("api/songs/{id}/comments")
    suspend fun getComments(
        @Path("id") id: Int,
        @Query("page") page: Int = 1
    ): Response<List<CommentResponse>>

    @POST("api/songs/{id}/comments")
    suspend fun addComment(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: AddCommentRequest
    ): Response<MessageResponse>

    @DELETE("api/songs/{id}/comments/{commentId}")
    suspend fun deleteComment(
        @Header("Authorization") token: String,
        @Path("id") songId: Int,
        @Path("commentId") commentId: Int
    ): Response<MessageResponse>

    // ── 저장(내 플레이리스트) ──────────────────────
    @POST("api/songs/{id}/save")
    suspend fun saveSong(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: SaveRequest
    ): Response<MessageResponse>

    @DELETE("api/songs/{id}/save")
    suspend fun unsaveSong(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<MessageResponse>

    @GET("api/saves")
    suspend fun getMySaves(
        @Header("Authorization") token: String
    ): Response<List<SaveResponse>>

    // ── 별점 ──────────────────────────────────────
    @POST("api/songs/{id}/rating")
    suspend fun rateSong(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: RatingRequest
    ): Response<RatingResponse>

    @GET("api/songs/{id}/rating")
    suspend fun getRating(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<RatingResponse>

    @DELETE("api/songs/{id}")
    suspend fun deleteSong(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<MessageResponse>

    // ── 차트 ──────────────────────────────────────
    @GET("api/charts")
    suspend fun getChart(
        @Query("period") period: String = "daily",
        @Query("genre_id") genreId: Int? = null
    ): Response<List<SongResponse>>

    // ── 아티스트 프로필 ────────────────────────────
    @PUT("api/profile")
    suspend fun saveProfile(
        @Header("Authorization") token: String,
        @Body body: UserProfileRequest
    ): Response<MessageResponse>

    @GET("api/profile")
    suspend fun getMyProfile(
        @Header("Authorization") token: String
    ): Response<UserProfileResponse>

    @GET("api/profile/{nickname}")
    suspend fun getUserProfile(
        @Path("nickname") nickname: String
    ): Response<UserProfileResponse>

    // ── 팔로우 ────────────────────────────────────
    @POST("api/follow/{nickname}")
    suspend fun followArtist(
        @Header("Authorization") token: String,
        @Path("nickname") nickname: String
    ): Response<FollowResponse>

    @DELETE("api/follow/{nickname}")
    suspend fun unfollowArtist(
        @Header("Authorization") token: String,
        @Path("nickname") nickname: String
    ): Response<FollowResponse>

    @GET("api/follow/{nickname}")
    suspend fun getFollowStatus(
        @Header("Authorization") token: String,
        @Path("nickname") nickname: String
    ): Response<FollowResponse>

    // ── 아티스트 통계 ─────────────────────────────
    @GET("api/stats/{nickname}")
    suspend fun getArtistStats(
        @Path("nickname") nickname: String
    ): Response<ArtistStatsResponse>
}
