package com.muedsa.upscayl.di

import com.muedsa.upscayl.configuration.RedisConfig
import com.muedsa.upscayl.listener.NetImageUpscaylListener
import com.muedsa.upscayl.service.*
import io.ktor.server.config.*
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun configModule(config: ApplicationConfig) = module(createdAtStart = true) {
    single<ApplicationConfig> { config }
    singleOf(::RedisConfig)
}

val listenerModule = module {
    singleOf(::NetImageUpscaylListener)
}

val appModule = module {
    singleOf(::RedisService)
    singleOf(::ImageDownloadService)
    singleOf(::ImageUploadService)
    singleOf(::UpscaylService)
    singleOf(::UpscaylImageProviderService)
}