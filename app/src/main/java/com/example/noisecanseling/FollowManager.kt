package com.example.noisecanseling

import androidx.compose.runtime.mutableStateMapOf
import com.example.noisecanseling.network.RetrofitClient
import com.example.noisecanseling.network.TokenManager

object FollowManager {
    // artistName -> isFollowing
    private val followState = mutableStateMapOf<String, Boolean>()

    fun isFollowing(artistName: String): Boolean = followState[artistName] ?: false

    fun setFollowing(artistName: String, value: Boolean) {
        followState[artistName] = value
    }

    suspend fun toggle(artistName: String): Boolean {
        val current = isFollowing(artistName)
        return try {
            val token = TokenManager.getBearerToken()
            val res = if (current) {
                RetrofitClient.api.unfollowArtist(token, artistName)
            } else {
                RetrofitClient.api.followArtist(token, artistName)
            }
            if (res.isSuccessful) {
                val newValue = !current
                followState[artistName] = newValue
                newValue
            } else current
        } catch (e: Exception) {
            current
        }
    }

    suspend fun loadFollowStatus(artistName: String) {
        try {
            val token = TokenManager.getBearerToken()
            val res = RetrofitClient.api.getFollowStatus(token, artistName)
            if (res.isSuccessful) {
                followState[artistName] = res.body()?.following ?: false
            }
        } catch (e: Exception) { /* ignore */ }
    }
}
