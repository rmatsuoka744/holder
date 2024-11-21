package Holder.models

data class CredentialResponse(
    val w3c_vc: W3cVc?,
    val sd_jwt_vc: SdJwtVc?
) {
    data class W3cVc(
        val format: String,
        val credential: String,
        val c_nonce: String,
        val c_nonce_expires_in: Int
    )

    data class SdJwtVc(
        val sd_jwt: String,
        val disclosures: List<String>,
        val key_binding_jwt: String
    )
}
