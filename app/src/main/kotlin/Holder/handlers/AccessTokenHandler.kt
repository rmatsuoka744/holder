package Holder.handlers

import Holder.models.AccessTokenResponse
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object AccessTokenHandler {
    private val client = OkHttpClient()
    private val gson = Gson()

    fun requestAccessToken(
        url: String,
        clientId: String,
        clientSecret: String,
        scope: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val requestBodyJson = mapOf(
            "grant_type" to "client_credentials",
            "client_id" to clientId,
            "client_secret" to clientSecret,
            "scope" to scope
        )
        val requestBody = gson.toJson(requestBodyJson).toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        val responseBody = it.body?.string().orEmpty()
                        val tokenResponse = gson.fromJson(responseBody, AccessTokenResponse::class.java)
                        onSuccess(tokenResponse.access_token)
                    } else {
                        onError(Exception("Unexpected response: $response"))
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                onError(e)
            }
        })
    }
}
