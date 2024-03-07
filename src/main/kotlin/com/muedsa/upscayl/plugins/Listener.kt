package com.muedsa.upscayl.plugins

import com.muedsa.upscayl.listener.NetImageUpscaylListener
import com.muedsa.upscayl.service.RedisService
import io.ktor.server.application.*
import org.koin.ktor.ext.get

fun Application.configureListener() {
    val redisService = get<RedisService>()
    redisService.addListener(get<NetImageUpscaylListener>())
}