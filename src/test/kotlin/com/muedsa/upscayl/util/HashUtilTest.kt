package com.muedsa.upscayl.util

import com.muedsa.upscayl.getResourceFile
import kotlin.test.Test

class HashUtilTest {
    @Test
    fun file_test() {
        val file = this.javaClass.classLoader.getResourceFile("image/test.png")
        val hash = file.sha256()
        assert(hash == "ee868a0db4df2edb39137ea4458618a219d26ede5d09c4949589f75e75482bcc")
    }
}