package com.muedsa.upscayl.service

import com.muedsa.upscayl.model.UpscaylTaskResult
import io.ktor.server.config.*
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

class UpscaylService(
    private val config: ApplicationConfig
) {

    private val logger = LoggerFactory.getLogger(UpscaylService::class.java)

    private val processPath: String = config.property("ktor.upscayl.processPath").getString()

    private val modelsDirPath: String = config.property("ktor.upscayl.modelsDirPath").getString()

    private val libsDirPath: String = config.property("ktor.upscayl.libsDirPath").getString()

    init {
        // check upscayl models directory
        Path(modelsDirPath).also {
            check(it.exists()) { "Upscayl models directory not found: $modelsDirPath" }
            check(it.isDirectory()) { "Upscayl models directory is not a directory: $modelsDirPath" }
        }
        // check upscayl
        val processBuilder = ProcessBuilder(processPath, "-v")
            .redirectErrorStream(true)
        if (libsDirPath.isNotBlank()) {
            processBuilder.environment()["LD_LIBRARY_PATH"] = "$libsDirPath:${processBuilder.environment()["LD_LIBRARY_PATH"]}"
        }
        logger.info("upscayl process check")
        logger.info("current working directory: ${System.getProperty("user.dir")}")
        logger.info("upscayl command: ${processBuilder.command().joinToString(" ")}")
        val process = processBuilder.start()
        val resultReader = BufferedReader(InputStreamReader(process.inputStream))
        val resultText = resultReader.use {
            it.readText()
        }
        process.waitFor()
        logger.info(resultText)
    }

    fun upscayl(
        source: String,
        target: String,
        scale: Int = 4,
        model: String = "realesrgan-x4plus",
        format: String = "png"
    ): UpscaylTaskResult {


        val processBuilder = ProcessBuilder(
            processPath,
            "-i", source,
            "-o", target,
            "-s", if(scale in 2..4) scale.toString() else "4",
            "-f", format,
            "-m", modelsDirPath,
            "-n", model,
            "-v"
        ).redirectErrorStream(true)
        processBuilder.environment().also {
            it["LD_LIBRARY_PATH"] = "./upscayl/linux:${it["LD_LIBRARY_PATH"]?:""}"
        }
        logger.debug("upscayl command: ${processBuilder.command().joinToString(" ")}")
        val startTime = System.currentTimeMillis()
        val process = processBuilder.start()
        val resultReader = BufferedReader(InputStreamReader(process.inputStream))
        val resultText = resultReader.use {
            it.readText()
        }
        val exitCode = process.waitFor()
        val endTime = System.currentTimeMillis()
        if (exitCode != 0) {
            logger.error(resultText)
        } else{
            logger.debug(resultText)
        }
        return UpscaylTaskResult(exitCode, resultText, startTime, endTime)
    }
}