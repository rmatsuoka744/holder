package Holder

import Holder.handlers.AccessTokenHandler
import Holder.handlers.CredentialHandler
import Holder.handlers.ProofJwtGenerator
import Holder.storage.Storage

import com.google.gson.Gson

fun main() {
    val gson = Gson() // Gson インスタンスを作成
    val privateKeyPath = "/home/rmatsuoka/Holder/app/src/main/kotlin/Holder/keys/client_p256_private.pem"

    while (true) {
        println("\nHolder CLI Application")
        println("1. Request Access Token")
        println("2. Request Verifiable Credential")
        println("3. Show Stored SD-JWT-VC")
        println("0. Exit")
        print("Select an option: ")

        when (readlnOrNull()?.toIntOrNull()) {
            1 -> {
                println("Requesting Access Token...")
                AccessTokenHandler.requestAccessToken(
                    url = "http://localhost:8080/token",
                    clientId = "TEST_CLIENT_ID_1",
                    clientSecret = "TEST_SECRET_1",
                    scope = "credential_issue",
                    onSuccess = {
                        println("Access Token: $it")
                        Storage.saveAccessToken(it) // ストレージに保存
                    },
                    onError = { e -> println("Failed to get Access Token: ${e.message}") }
                )
                Thread.sleep(1000) // 非同期のレスポンスを待つ簡易的な方法
            }

            2 -> {
                val accessToken = Storage.loadAccessToken()

                if (accessToken.isNullOrEmpty()) {
                    println("Access Token is not available. Please request an Access Token first.")
                    continue
                }

                val proofJwt = ProofJwtGenerator.generateProofJwt(privateKeyPath)

                println("Requesting Verifiable Credential...")
                CredentialHandler.requestVerifiableCredential(
                    url = "http://localhost:8080/credential",
                    accessToken = accessToken,
                    cnfJwk = mapOf(
                        "alg" to "ES256",
                        "crv" to "P-256",
                        "kty" to "EC",
                        "use" to "sig",
                        "x" to "XcW-sLJaCg0FB__Pgpg8nFZialZKB7goW_ohTZi2zyY",
                        "y" to "Vu868_dULyyXyi2uurc-QmxY2jIKy-DIxAn0wzhcyXk"
                    ),
                    proofJwt = proofJwt,
                    onSuccess = {
                        val sdJwtVc = CredentialHandler.getSdJwtVc()
                        println("VC successfully requested!")
                        println("SD-JWT-VC: $sdJwtVc")
                        if (sdJwtVc != null) {
                            val sdJwtVcJson = gson.toJson(sdJwtVc)
                            Storage.saveVerifiableCredential(sdJwtVcJson)
                        }
                    },
                    onError = { e -> println("Failed to request VC: ${e.message}") }
                )
                Thread.sleep(1000)
            }

            3 -> {
                // ストレージからSD-JWT-VCを読み込む
                val storedVc = Storage.loadVerifiableCredential()
                if (storedVc != null) {
                    println("Stored SD-JWT-VC: $storedVc")
                } else {
                    println("No SD-JWT-VC found in storage.")
                }
            }

            0 -> {
                println("Exiting...")
                break
            }

            else -> println("Invalid option. Please try again.")
        }
    }
}
