package com.muedsa.upscayl.util

import java.io.File
import java.io.InputStream
import java.security.MessageDigest

object HashUtil {
    const val BUFFER_LENGTH = 1024

    fun hash(input: String, algorithm: String): String {
        return hash(input.toByteArray(), algorithm)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun hash(input: ByteArray, algorithm: String): String {
        return MessageDigest.getInstance(algorithm)
            .digest(input)
            .toHexString()
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun hash(input: InputStream, algorithm: String): String {
        val md = MessageDigest.getInstance(algorithm)
        input.use {
            val buffer = ByteArray(BUFFER_LENGTH)
            var bytesRead = it.read(buffer)
            while (bytesRead != -1) {
                md.update(buffer, 0, bytesRead)
                bytesRead = it.read(buffer)
            }
        }
        return md.digest().toHexString()
    }

    fun sha256(input: String): String {
        return hash(input, "SHA-256")
    }

    fun sha256(input: ByteArray): String {
        return hash(input, "SHA-256")
    }

    fun sha256(input: InputStream): String {
        return hash(input, "SHA-256")
    }
}

fun File.sha256(): String {
    return HashUtil.sha256(inputStream())
}