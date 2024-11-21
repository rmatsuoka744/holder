package Holder

import Holder.handlers.AccessTokenHandler
import Holder.handlers.CredentialHandler

fun main() {
    var accessToken: String? = null // アクセストークンを保持する変数

    while (true) {
        println("\nHolder CLI Application")
        println("1. Request Access Token")
        println("2. Request Verifiable Credential")
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
                        accessToken = it // アクセストークンを保存
                    },
                    onError = { e -> println("Failed to get Access Token: ${e.message}") }
                )
                Thread.sleep(1000) // 非同期のレスポンスを待つ簡易的な方法（本番コードでは要改善）
            }

            2 -> {
                if (accessToken.isNullOrEmpty()) {
                    println("Access Token is not available. Please request an Access Token first.")
                    continue
                }

                println("Requesting Verifiable Credential...")
                CredentialHandler.requestVerifiableCredential(
                    url = "http://localhost:8080/credential",
                    accessToken = accessToken!!, // 保存したアクセストークンを使用
                    cnfJwk = mapOf(
                        "alg" to "EdDSA",
                        "crv" to "Ed25519",
                        "kty" to "OKP",
                        "use" to "sig",
                        "x" to "-w76fv0jlTZo3H6mtdcJrJZfJ4Ltm2MJi09V_zxM3Vo"
                    ),
                    proofJwt = "eyJ0eXAiOiJvcGVuaWQ0dmNpLXByb29mK2p3dCIsImFsZyI6IkVkRFNBIiwiandrIjp7InVzZSI6InNpZyIsImt0eSI6Ik9LUCIsImNydiI6IkVkMjU1MTkiLCJ4IjoiLXc3NmZ2MGpsVFpvM0g2bXRkY0pySlpmSjRMdG0yTUppMDlWX3p4TTNWbyJ9fQ.eyJleHAiOjE3MzIxODExNTEsImlhdCI6MTczMjE3OTk1MSwibm9uY2UiOiJ0ZXN0X25vbmNlIn0.CP0UsqTBkWi3EyXcokd8CbKI79j2QFTfc8HUkqc8sFSfGQk_qegRQeZCu_bGT8tez0ZS2zrna6kbEO_Qr68hAg",
                    onSuccess = {
                        println("VC successfully requested!")
                        println("SD-JWT-VC: ${CredentialHandler.getSdJwtVc()}")
                    },
                    onError = { e -> println("Failed to request VC: ${e.message}") }
                )
                Thread.sleep(1000) // 非同期のレスポンスを待つ簡易的な方法（本番コードでは要改善）
            }

            0 -> {
                println("Exiting...")
                break
            }

            else -> println("Invalid option. Please try again.")
        }
    }
}
