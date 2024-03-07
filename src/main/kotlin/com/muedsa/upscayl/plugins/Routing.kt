package com.muedsa.upscayl.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

//        get("/upscayl") {
//            val params = call.request.queryParameters
//            val text = params["text"] ?: ""
//            val lang = params["lang"] ?: ""
//            val result = upscaylService.upscayl(UpscaylParams(text, lang))
//            call.respondText(result)
//        }
    }
}
