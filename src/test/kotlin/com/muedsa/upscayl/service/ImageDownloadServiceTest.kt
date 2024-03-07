package com.muedsa.upscayl.service

import io.ktor.server.testing.*
import org.koin.ktor.ext.get
import kotlin.test.Test

class ImageDownloadServiceTest {
    @Test
    fun download_test() = testApplication {
        application {
            val service = get<ImageDownloadService>()
            val result = service.downloadImage("https://samples-files.com/samples/Images/jpg/3840-2160-sample.jpg")
            println(result)
        }
    }
}