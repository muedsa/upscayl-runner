package com.muedsa.upscayl.service

import com.muedsa.upscayl.model.ExistUpscaylImageResp
import com.muedsa.upscayl.model.ImageUrlAlias
import com.muedsa.upscayl.model.ProvideUpscaylImage
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking

class UpscaylImageProviderService(
    private val config: ApplicationConfig
) {
    private val providerHost: String = config.property("ktor.image.provider.host").getString()
    private val providerToken: String = config.property("ktor.image.provider.token").getString()

    private val client = HttpClient(CIO) {
        install(Logging)
        install(ContentNegotiation) {
            json()
        }
        defaultRequest {
            headers.appendIfNameAbsent(HttpHeaders.Authorization, providerToken)
        }
        expectSuccess = true
    }

    fun updateImageHash(data: ImageUrlAlias, traceId: String = ""): ExistUpscaylImageResp {
        return runBlocking {
            client.post(urlString = "$providerHost/updateImageHash") {
                contentType(ContentType.Application.Json)
                setBody(data)
                header(HttpHeaders.Authorization, providerToken)
                header("X-Request-ID", traceId)
            }.body<ExistUpscaylImageResp>()
        }
    }

    fun provide(data: ProvideUpscaylImage) {
        runBlocking {
            client.post(urlString = "$providerHost/provide") {
                contentType(ContentType.Application.Json)
                setBody(data)
                header(HttpHeaders.Authorization, providerToken)
                header("X-Request-ID", data.traceId)
            }
        }
    }
}