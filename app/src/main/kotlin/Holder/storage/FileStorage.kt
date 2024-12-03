package Holder.storage

import java.io.File

object Storage {
    private const val TOKEN_FILE = "/home/rmatsuoka/Holder/app/src/main/kotlin/Holder/filestorage/access_token.json"
    private const val VC_FILE = "/home/rmatsuoka/Holder/app/src/main/kotlin/Holder/filestorage/verifiable_credential.json"

    fun saveAccessToken(token: String) {
        val json = """{"access_token": "$token"}"""
        File(TOKEN_FILE).writeText(json)
    }

    fun loadAccessToken(): String? {
        val file = File(TOKEN_FILE)
        return if (file.exists()) {
            val json = file.readText()
            Regex("\"access_token\":\\s*\"(.*?)\"").find(json)?.groupValues?.get(1)
        } else {
            null
        }
    }

    fun saveVerifiableCredential(vc: String) {
        File(VC_FILE).writeText(vc)
    }

    fun loadVerifiableCredential(): String? {
        val file = File(VC_FILE)
        return if (file.exists()) {
            file.readText().trim('"') 
        } else {
            null
        }
    }
}
