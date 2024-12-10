package Holder

import Holder.handlers.AccessTokenHandler
import Holder.handlers.CredentialHandler
import Holder.handlers.ProofJwtGenerator
import Holder.handlers.VcProcessor
import Holder.handlers.VPGenerator
import Holder.storage.Storage
import Holder.utils.Utils.loadPrivateKey

import com.google.gson.Gson

fun main() {
    val gson = Gson() // Gson インスタンスを作成
    val privateKeyPath = "/home/rmatsuoka/Holder/app/src/main/kotlin/Holder/keys/client_p256_private.pem"

    while (true) {
        println("\nHolder CLI Application")
        println("1. Request Access Token")
        println("2. Request Verifiable Credential")
        println("3. Show Stored SD-JWT-VC")
        println("4. Generate VP")
        println("0. Exit")
        print("Select an option: ")

        when (readlnOrNull()?.toIntOrNull()) {
            1 -> {
                println("Requesting Access Token...")
                AccessTokenHandler.requestAccessToken(
                    url = "http://localhost:8080/token",
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

            4 -> {
                // VPを生成
                println("Generating Verifiable Presentation...")

                // 保存された VC を読み込む
                val storedVc = Storage.loadVerifiableCredential()
                if (storedVc.isNullOrEmpty()) {
                    println("No VC found. Please request a VC first.")
                    continue
                }

                try {
                    // VC を分割
                    val (sdJwt, disclosures) = VcProcessor.parseVc(storedVc)

                    // ディスクロージャを選択（例: 全て開示）
                    println("Available Disclosures: $disclosures")
                    println("Enter indices of disclosures to include (comma-separated, e.g., '0,2'):")
                    val indicesInput = readlnOrNull() ?: ""
                    val selectedIndices = indicesInput.split(",").mapNotNull { it.toIntOrNull() }
                    val selectedDisclosures = selectedIndices.mapNotNull { disclosures.getOrNull(it) }

                    // VP の生成
                    val vp = VPGenerator.generateVP(sdJwt, selectedDisclosures, privateKeyPath)

                    // VP を表示
                    println("Generated Verifiable Presentation:")
                    println(vp)
                } catch (e: Exception) {
                    println("Failed to generate VP: ${e.message}")
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
