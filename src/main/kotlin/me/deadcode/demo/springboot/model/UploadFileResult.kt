package me.deadcode.demo.springboot.model

data class UploadFileResult(
    val success: Int = 0,
    val uuid: String? = null,
    val errorCode: Int? = null,
    val error: String? = null,
)
