package com.muedsa.upscayl.service

import com.muedsa.upscayl.model.UpscaylTaskResult
import com.muedsa.upscayl.util.OSType
import com.muedsa.upscayl.util.OSUtil
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader

class UpscaylService {

    private val logger = LoggerFactory.getLogger(UpscaylService::class.java)

    private val processPath: String = when(OSUtil.OS) {
        OSType.WINDOWS -> "upscayl/win/upscayl-bin.exe"
        OSType.LINUX -> "upscayl/linux/upscayl-bin"
        OSType.MACOS -> throw IllegalStateException("not support for macos")
    }

    init {
        // check upscayl
        System.getProperty("user.dir").also {
            logger.info("current work dir: $it")
        }
        ProcessBuilder(processPath, "-v").start().waitFor()
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
            "-m", "../models",
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