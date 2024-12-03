package Holder.handlers

import com.google.gson.Gson
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.security.MessageDigest
import java.security.PrivateKey
import java.util.*
import Holder.utils.Utils.loadPrivateKey
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.Duration

object VcProcessor {
    /**
     * SD-JWT VC を分割し、SD-JWT とディスクロージャを抽出する
     * @param vc SD-JWT VC 文字列 ("sdjwt~dis1~dis2~" 形式)
     * @return Pair<SD-JWT, List<Disclosure>> SD-JWT とディスクロージャのペア
     */
    fun parseVc(vc: String): Pair<String, List<String>> {
        // "~" で分割し、最後の空要素を削除
        val parts = vc.split("~").filter { it.isNotEmpty() }

        // 最初の要素が SD-JWT、それ以降がディスクロージャ
        val sdJwt = parts.firstOrNull() ?: throw IllegalArgumentException("Invalid VC format: Missing SD-JWT")
        val disclosures = parts.drop(1)

        return Pair(sdJwt, disclosures)
    }
}

object VPGenerator {
    private val gson = Gson()

    // ディスクロージャを選択
    fun selectDisclosures(allDisclosures: List<String>, indicesToInclude: List<Int>): List<String> {
        return indicesToInclude.mapNotNull { index ->
            allDisclosures.getOrNull(index)
        }
    }

    private fun generateKeyBindingJwt(
        sdJwt: String,
        disclosures: List<String>, // 選択的開示
        privateKey: PrivateKey,
        audience: String = "https://fujita-verifier.example.com",
        nonce: String = UUID.randomUUID().toString()
    ): String {
        val issuedAt = System.currentTimeMillis() / 1000 // UNIX timestamp in seconds

        // SD-JWT と選択した disclosure を ~ で連結
        val presentation = buildString {
            append(sdJwt)
            disclosures.forEach { disclosure ->
                append("~")
                append(disclosure)
            }
            append("~")
        }
        // println("Raw presentation: $presentation")

        // US-ASCII バイト列に変換
        val asciiBytes = presentation.toByteArray(StandardCharsets.US_ASCII)
        // SHA-256 でハッシュ化して Base64URL エンコード
        val sdHash = MessageDigest.getInstance("SHA-256").digest(asciiBytes)
        val sdHashBase64Url = Base64.getUrlEncoder().withoutPadding().encodeToString(sdHash)

        //exp 生成
        val now = Instant.now()
    
        // 指定した時間を加算
        val exp = now.plus(Duration.ofHours(8760)).epochSecond

        // JWT クレームの生成
        val claims = mapOf(
            "aud" to audience,
            "iat" to issuedAt,
            "nonce" to nonce,
            "sd_hash" to sdHashBase64Url,
            "exp" to exp
        )

        // Key Binding JWT の生成
        return Jwts.builder()
            .setHeaderParam("typ", "kb+jwt")
            .setClaims(claims)
            .signWith(privateKey, SignatureAlgorithm.ES256)
            .compact()
    }

    // VP の生成
    fun generateVP(
        sdJwt: String,
        selectedDisclosures: List<String>,
        privateKeyPath: String
    ): String {
        val privateKey = loadPrivateKey(privateKeyPath)
        val keyBindingJwt = generateKeyBindingJwt(sdJwt, selectedDisclosures, privateKey)

        return listOf(sdJwt, *selectedDisclosures.toTypedArray(), keyBindingJwt)
            .joinToString("~") // `~` で連結
    }
}
