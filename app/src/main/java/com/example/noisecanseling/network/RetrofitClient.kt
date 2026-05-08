package com.example.noisecanseling.network

import android.content.Context
import android.content.SharedPreferences
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // 실제 기기에서 PC 서버로 접근할 때는 PC의 로컬 IP 사용
    private const val BASE_URL = "http://172.21.93.54:3000/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    // 스트리밍 URL 생성
    fun streamUrl(songId: Int): String = "${BASE_URL}api/songs/$songId/stream"

    // 커버 이미지 URL 생성
    fun coverUrl(coverPath: String?): String? {
        if (coverPath == null) return null
        return "$BASE_URL${coverPath.replace("\\", "/")}"
    }
}

// 토큰 저장/불러오기
object TokenManager {
    private const val PREF_NAME = "auth_prefs"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_NICKNAME = "nickname"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(token: String, nickname: String) {
        prefs.edit().putString(KEY_TOKEN, token).putString(KEY_NICKNAME, nickname).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getNickname(): String? = prefs.getString(KEY_NICKNAME, null)

    fun getBearerToken(): String = "Bearer ${getToken() ?: ""}"

    fun isLoggedIn(): Boolean = getToken() != null

    fun logout() {
        prefs.edit().clear().apply()
    }
}
