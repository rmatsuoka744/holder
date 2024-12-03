package Holder.handlers

import com.google.gson.Gson
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import Holder.utils.Utils.loadPrivateKey

object ProofJwtGenerator {
    private val gson = Gson()

    /**
     * 現在のタイムスタンプ (iat) と有効期限 (exp) を生成する
     */
    private fun generateTimestamps(): Pair<Long, Long> {
        val iat = System.currentTimeMillis() / 1000 // 現在時刻を秒に変換
        val exp = iat + 1200 // 有効期限 (20分後)
        return iat to exp
    }

    /**
     * OpenID4VCI Proof JWT を生成する
     */
    fun generateProofJwt(privateKeyPath: String): String {
        // ヘッダー
        val header = mapOf(
            "typ" to "openid4vci-proof+jwt",
            "alg" to "ES256",
            "jwk" to mapOf(
                "use" to "sig",
                "kty" to "EC",
                "crv" to "P-256",
                "x" to "XcW-sLJaCg0FB__Pgpg8nFZialZKB7goW_ohTZi2zyY",
                "y" to "Vu868_dULyyXyi2uurc-QmxY2jIKy-DIxAn0wzhcyXk"
            )
        )

        // ペイロード
        val (iat, exp) = generateTimestamps()
        val payload = mapOf(
            "exp" to exp,
            "iat" to iat,
            "nonce" to "test_nonce" // ダミーのノンス (ランダム生成可)
        )

        // プライベートキーのロード
        val privateKey = loadPrivateKey(privateKeyPath)

        // JWT の生成
        return Jwts.builder()
            .setHeader(header)
            .setClaims(payload)
            .signWith(privateKey, SignatureAlgorithm.ES256)
            .compact()
    }
}
