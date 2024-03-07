package com.muedsa.upscayl.service

import com.muedsa.upscayl.getResourceFile
import io.ktor.server.testing.*
import org.koin.ktor.ext.get
import kotlin.test.Test

class imageUploadServiceTest {

    @Test
    fun upload_test() = testApplication {
        application {
            val service = get<ImageUploadService>()
            val file = environment.classLoader.getResourceFile("image/test.png")
            service.upload(file)
        }
    }

}