package com.muedsa.upscayl.listener

import com.muedsa.upscayl.model.NetworkImageUpscaylParams
import com.muedsa.upscayl.model.ProvideUpscaylImage
import com.muedsa.upscayl.service.*
import com.muedsa.upscayl.util.sha256
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.io.File

class NetImageUpscaylListener(
    private val redisService: RedisService,
    private val imageDownloadService: ImageDownloadService,
    private val upscaylService: UpscaylService,
    private val imageUploadService: ImageUploadService,
    private val upscaylImageProviderService: UpscaylImageProviderService
) : RedisChannelListener("NETWORK_IMAGE_UPSCAYL") {

    private val logger = LoggerFactory.getLogger(NetImageUpscaylListener::class.java)

    override fun message(message: String) {
        val (guid, url, scale, model, format) = Json.decodeFromString<NetworkImageUpscaylParams>(message)
        MDC.put("traceId", guid)
        if (redisService.setNx("MESSAGE_LOCK:$guid", "processing", 60L)) {
            println("NetImageUpscaylListener: $message")
            var sourceFile: File? = null
            var targetFile: File? = null
            try {
                sourceFile = imageDownloadService.downloadImage(url)
                targetFile = imageDownloadService.getAvailableFile(".$format")
                val result = upscaylService.upscayl(
                    sourceFile.path, targetFile.path, scale, model, format
                )
                if (result.success) {
                    val resp = imageUploadService.upload(file = targetFile)
                    if (!resp.files.isNullOrEmpty()) {
                        //redisService
                        val provideUpscaylImage = ProvideUpscaylImage(
                            traceId = guid,
                            sourceUrl = url,
                            sourceHash = sourceFile.sha256(),
                            upscaylUrl = resp.files[0],
                            taskResult = result
                        )
                        upscaylImageProviderService.provide(provideUpscaylImage)
                    } else {
                        logger.error("NetImageUpscaylListener: ${resp.error} ${resp.code}")
                    }
                }
            } catch (t: Throwable) {
                logger.error("NetImageUpscaylListener: ", t)
            } finally {
                catchBlocks(
                    { sourceFile?.delete() },
                    { targetFile?.delete() },
                    { redisService.del("MESSAGE_LOCK:$guid") },
                    { redisService.del("UPSCAYL_TASK_LOCK:$url") }
                )
            }
        }
    }

    private fun catchBlocks(vararg blocks: () -> Unit) {
        for (block in blocks) {
            try {
                block.invoke()
            } catch (t: Throwable) {
                logger.error("NetImageUpscaylListener: ", t)
            }
        }
    }
}