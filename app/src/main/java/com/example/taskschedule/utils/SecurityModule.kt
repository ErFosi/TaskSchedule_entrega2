package com.example.taskschedule.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.BLOCK_MODE_GCM
import android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE
import android.security.keystore.KeyProperties.PURPOSE_DECRYPT
import android.security.keystore.KeyProperties.PURPOSE_ENCRYPT
import android.security.keystore.KeyProperties.*
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

    /************************************************************************
     * Clase que se encarga del cifrado y descifrado AESCipher
     * codigo obtenido de internet, mirar bibiliografía documentación.
    *************************************************************************/


class NoCryptographicKeyException : Exception()

class AESCipher @Inject constructor() {


    companion object {
        private const val IV_SEPARATOR = "]"
    }


    private val provider = "AndroidKeyStore"
    private val keyStore by lazy { KeyStore.getInstance(provider).apply { load(null) } }
    private val keyGenerator by lazy { KeyGenerator.getInstance(KEY_ALGORITHM_AES, provider) }


    private val cipher by lazy { Cipher.getInstance("AES/GCM/NoPadding") }


    private fun generateSecretKey(keyAlias: String): SecretKey {
        return keyGenerator.apply {
            init(
                KeyGenParameterSpec
                    .Builder(keyAlias, PURPOSE_ENCRYPT or PURPOSE_DECRYPT)
                    .setBlockModes(BLOCK_MODE_GCM)
                    .setEncryptionPaddings(ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
        }.generateKey()
    }

    private fun getSecretKey(keyAlias: String): SecretKey = (keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey



    fun encryptData(keyAlias: String, data: String): String {
        cipher.init(Cipher.ENCRYPT_MODE, generateSecretKey(keyAlias))

        val encodedIV = Base64.encodeToString(cipher.iv, Base64.DEFAULT)
        val encodedData = Base64.encodeToString(cipher.doFinal(data.toByteArray()), Base64.DEFAULT)

        return "$encodedIV$IV_SEPARATOR$encodedData"
    }


    @Throws(NoCryptographicKeyException::class)
    private fun decryptData(keyAlias: String, encryptedData: ByteArray, iv: ByteArray): String {
        try {

            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(keyAlias), GCMParameterSpec(128, iv))
        } catch (e: NullPointerException) {
            throw NoCryptographicKeyException()
        }

        return String(cipher.doFinal(encryptedData))
    }


    @Throws(NoCryptographicKeyException::class)
    fun decryptData(keyAlias: String, encryptedDataWithIV: String): String {

        val split = encryptedDataWithIV.split(IV_SEPARATOR.toRegex())
        if (split.size != 2) throw IllegalArgumentException("Passed data is not valid. It does not define a valid IV.")

        val iv = Base64.decode(split[0], Base64.DEFAULT)
        val encryptedDataBytes = Base64.decode(split[1], Base64.DEFAULT)

        return decryptData(keyAlias, encryptedDataBytes, iv)
    }
}
