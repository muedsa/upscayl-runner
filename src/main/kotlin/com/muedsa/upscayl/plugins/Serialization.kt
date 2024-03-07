package com.muedsa.upscayl.plugins

import com.muedsa.upscayl.model.NetworkImageUpscaylParams
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID
import kotlin.random.Random

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
    routing {
        get("/json/kotlinx-serialization") {
            call.respond(NetworkImageUpscaylParams(UUID.randomUUID().toString(), "https://example.com/image.jpg"))
        }
    }
}
