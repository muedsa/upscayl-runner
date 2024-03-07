package com.muedsa.upscayl.util

object OSUtil {
    private val osName: String = System.getProperty("os.name")
    val OS = when {
        osName == "Mac OS X" -> OSType.MACOS
        osName.startsWith("Win") -> OSType.WINDOWS
        osName.startsWith("Linux") -> OSType.LINUX
        else -> error("Unsupported OS: $osName")
    }
}

enum class OSType {
    MACOS,
    WINDOWS,
    LINUX
}