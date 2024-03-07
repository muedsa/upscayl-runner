package com.muedsa.upscayl

import com.muedsa.upscayl.di.KoinShutdownDispatcher
import com.muedsa.upscayl.di.appModule
import com.muedsa.upscayl.di.configModule
import com.muedsa.upscayl.di.listenerModule
import com.muedsa.upscayl.plugins.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    install(Koin) {
        slf4jLogger()
        modules(configModule(environment.config), listenerModule, appModule)
    }

    configureMonitoring()
    configureSerialization()
    configureRouting()
    configureListener()
    KoinShutdownDispatcher.complete(environment)
}

