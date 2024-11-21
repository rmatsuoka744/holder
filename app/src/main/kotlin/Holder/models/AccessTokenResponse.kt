package Holder.models

data class AccessTokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val scope: String,
    val c_nonce: String,
    val c_nonce_expires_in: Int
)
