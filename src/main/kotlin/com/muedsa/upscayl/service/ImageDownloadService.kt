package com.muedsa.upscayl.service

import com.muedsa.upscayl.di.KoinShutdownDispatcher
import com.muedsa.upscayl.util.ImageFormatValidator
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Paths

class ImageDownloadService(
    private val config: ApplicationConfig
) {
    private val imageTempDir: File by lazy {
        Paths.get(config.property("ktor.image.temp.dir").getString()).toAbsolutePath().toFile()
    }

    private val downloadLimit: Long =
        config.property("ktor.image.download.limit").getString().toLongOrNull() ?: Long.MAX_VALUE

    private val client = HttpClient(CIO)

    init {
        KoinShutdownDispatcher.register { client.close() }
        imageTempDir.mkdirs()
    }

    fun getAvailableFile(suffix: String): File {
        return File.createTempFile("temp_", suffix, imageTempDir)
    }

    fun downloadImage(url: String): File {
        return runBlocking {
            val channel: ByteReadChannel = client.get {
                url(url)
                method = HttpMethod.Get
            }.bodyAsChannel()
            val buffer = ByteArray(ImageFormatValidator.NECESSARY_MAGIC_LENGTH)
            val suffix = checkImage(channel, buffer)
            val file = getAvailableFile(".$suffix")
            val fileWriteChannel = file.writeChannel()
            fileWriteChannel.writeFully(buffer)
            channel.copyAndClose(dst = fileWriteChannel, limit = downloadLimit)
            check(channel.availableForRead == 0) { "Exceeding maximum image download limit $downloadLimit bytes" }
            return@runBlocking file
        }
    }
}

suspend fun checkImage(channel: ByteReadChannel, buffer: ByteArray): String {
    channel.readFully(buffer)
    return if (ImageFormatValidator.validPNG(buffer)) {
        "png"
    } else if (ImageFormatValidator.validJPEG(buffer)) {
        "jpg"
    } else if (ImageFormatValidator.validWEBP(buffer)) {
        "webp"
    } else {
        throw IllegalArgumentException("Invalid image format")
    }
}