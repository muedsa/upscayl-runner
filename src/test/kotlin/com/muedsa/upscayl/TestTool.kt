package com.muedsa.upscayl

import java.io.File

fun ClassLoader.getResourceFile(name: String): File {
    val url = this.getResource(name) ?: throw IllegalArgumentException("Resource not found: $name")
    return File(url.toURI())
}