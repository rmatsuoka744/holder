package Holder

import Holder.handlers.AccessTokenHandler
import Holder.handlers.CredentialHandler
import Holder.storage.Storage

import com.google.gson.Gson 

fun main() {
    val gson = Gson() // Gsonインスタンスを作成

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

                println("Requesting Verifiable Credential...")
                CredentialHandler.requestVerifiableCredential(
                    url = "http://localhost:8080/credential",
                    accessToken = accessToken, // 保存したアクセストークンを使用
                    cnfJwk = mapOf(
                        "alg" to "EdDSA",
                        "crv" to "Ed25519",
                        "kty" to "OKP",
                        "use" to "sig",
                        "x" to "-w76fv0jlTZo3H6mtdcJrJZfJ4Ltm2MJi09V_zxM3Vo"
                    ),
                    proofJwt = "eyJ0eXAiOiJvcGVuaWQ0dmNpLXByb29mK2p3dCIsImFsZyI6IkVkRFNBIiwiandrIjp7InVzZSI6InNpZyIsImt0eSI6Ik9LUCIsImNydiI6IkVkMjU1MTkiLCJ4IjoiLXc3NmZ2MGpsVFpvM0g2bXRkY0pySlpmSjRMdG0yTUppMDlWX3p4TTNWbyJ9fQ.eyJleHAiOjE3MzIyNDU2NjMsImlhdCI6MTczMjI0NDQ2Mywibm9uY2UiOiJ0ZXN0X25vbmNlIn0.qulCKRx1PXS5so96FhHG7dXWPbLkeArsEO28cNmOTwxMRWrbf5JkxrKLCxDLwSMqDmI_2kyzCCKS1pG4IvHsAg",
                    onSuccess = {
                        val sdJwtVc = CredentialHandler.getSdJwtVc()
                        println("VC successfully requested!")
                        println("SD-JWT-VC: $sdJwtVc")
                        if (sdJwtVc != null) {
                            val sdJwtVcJson = gson.toJson(sdJwtVc) // SdJwtVcオブジェクトをJSON文字列に変換
                            Storage.saveVerifiableCredential(sdJwtVcJson) // 文字列として保存
                        }
                    },
                    onError = { e -> println("Failed to request VC: ${e.message}") }
                )
                Thread.sleep(1000) // 非同期のレスポンスを待つ簡易的な方法
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
