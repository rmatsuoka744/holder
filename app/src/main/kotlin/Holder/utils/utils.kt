package Holder.utils

import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*

object Utils {
    /**
     * プライベートキーをファイルからロードする
     * @param privateKeyPath 鍵ファイルのパス
     * @return PrivateKey
     */
    fun loadPrivateKey(privateKeyPath: String): PrivateKey {
        val keyBytes = Files.readAllBytes(Paths.get(privateKeyPath))
        val keyPem = String(keyBytes)
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\n", "")
            .replace("\r", "")
        val decodedKey = Base64.getDecoder().decode(keyPem)
        val keySpec = PKCS8EncodedKeySpec(decodedKey)
        val keyFactory = KeyFactory.getInstance("EC")
        return keyFactory.generatePrivate(keySpec)
    }
}
