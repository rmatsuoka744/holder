package Holder.models

data class CredentialResponse(
    val format: String,
    val credential: String,
    val c_nonce: String,
    val c_nonce_expires_in: Int
)