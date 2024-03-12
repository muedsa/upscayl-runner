package com.muedsa.upscayl.service

import com.muedsa.upscayl.model.ZiplineUploadResp
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.File

class ImageUploadService(
    private val config: ApplicationConfig
) {

    private val uploadUrl: String = config.property("ktor.image.upload.url").getString()
    private val uploadToken: String = config.property("ktor.image.upload.token").getString()


    private val client = HttpClient(CIO) {
        install(Logging)
        defaultRequest {
            headers.appendIfNameAbsent(HttpHeaders.Authorization, uploadToken)
        }
        expectSuccess = true
    }

    fun upload(file: File): ZiplineUploadResp {
        return runBlocking {
            check(file.isFile && file.canRead())
            val contentType = file.name.substringAfterLast(".").lowercase().let {
                when(it) {
                    "png" -> ContentType.Image.PNG.toString()
                    "jpg", "jpeg" -> ContentType.Image.JPEG.toString()
                    "webp" -> "image/webp"
                    else -> throw IllegalArgumentException("unsupported file type: $it")
                }
            }
            val resp = client.post(urlString = uploadUrl) {
                setBody(MultiPartFormDataContent(
                    formData {
                        append("file", file.readBytes(), Headers.build {
                            append(HttpHeaders.ContentType, contentType)
                            append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                        })
                    }
                ))
            }.bodyAsText()
            return@runBlocking Json.Default.decodeFromString<ZiplineUploadResp>(resp)
        }
    }
}