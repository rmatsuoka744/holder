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
        scope: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val requestBodyJson = mapOf(
            "vct" to "https://fujita-issuer.example.com/vc/mynumber",
            "grant_type" to "client_credentials",
            "auth_data" to mapOf(
                "identificationParam" to "dummy",
                "name" to "fujita taro",
                "gender" to "male",
                "address" to "aichi",
                "birthdate" to "2018-10-10"
            ),
            "scope" to scope
        )
        // val requestBodyJson = mapOf(
        //     "vct" to "https://fujita-issuer.example.com/vc/patient-id",
        //     "grant_type" to "client_credentials",
        //     "auth_data" to mapOf(
        //         "patient_id" to "patient:12345",
        //         "did" to "did:12345"
        //     ),
        //     "scope" to scope
        // )
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
