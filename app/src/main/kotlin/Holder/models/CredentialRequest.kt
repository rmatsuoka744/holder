package Holder.models

data class CredentialRequest(
    val format: String,
    val types: List<String>,
    val cnf: Map<String, Any>,
    val proof: Proof
) {
    data class Proof(
        val proof_type: String,
        val jwt: String
    )
}
