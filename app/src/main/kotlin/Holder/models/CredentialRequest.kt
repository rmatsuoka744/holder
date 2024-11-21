package Holder.models

data class CredentialRequest(
    val formats: List<String>,
    val types: List<String>,
    val cnf: Map<String, Any>,
    val proof: Proof
) {
    data class Proof(
        val proof_type: String,
        val jwt: String
    )
}
