package Holder.models

data class AccessTokenRequest(
    val vct: String,
    val grant_type: String = "client_credentials",
    val client_id: String,
    val client_secret: String,
    val scope: String
)
