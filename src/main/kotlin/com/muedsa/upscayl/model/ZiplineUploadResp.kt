package com.muedsa.upscayl.model

import kotlinx.serialization.Serializable

@Serializable
class ZiplineUploadResp : ZiplineErrorResp(){
    val files: List<String>? = null
    val folder: Int? = null
}