package Holder.handlers

import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import Holder.models.CredentialRequest
import Holder.models.CredentialResponse

object CredentialHandler {
    private val client = OkHttpClient()
    private val gson = Gson()
    private var vc: String? = null // VCを保持

    fun requestVerifiableCredential(
        url: String,
        accessToken: String, // アクセストークンを追加
        cnfJwk: Map<String, Any>,
        proofJwt: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val requestData = CredentialRequest(
            format = "sd_jwt_vc",
            types = listOf("VerifiableCredential", "UniversityDegreeCredential"),
            cnf = mapOf("jwk" to cnfJwk),
            proof = CredentialRequest.Proof(proof_type = "jwt", jwt = proofJwt)
        )

        val requestBody = gson.toJson(requestData).toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Authorization", "Bearer $accessToken") // アクセストークンをヘッダーに設定
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        try {
                            val responseBody = it.body?.string().orEmpty()
                            val credentialResponse = gson.fromJson(responseBody, CredentialResponse::class.java)
                            vc = credentialResponse.credential // VCを保持
                            onSuccess()
                        } catch (e: Exception) {
                            onError(e)
                        }
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

    fun getSdJwtVc(): String? {
        return vc
    }
}
