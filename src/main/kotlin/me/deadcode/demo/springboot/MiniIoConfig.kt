package me.deadcode.demo.springboot

import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MiniIoConfig {
    @Value("\${minio.url}")
    private val url: String? = null

    @Value("\${minio.accesskey}")
    private val accessKey: String? = null

    @Value("\${minio.secretkey}")
    private val secretKey: String? = null

    @Bean
    fun minioClient(): MinioClient {
        return MinioClient.builder()
            .endpoint(url)
            .credentials(accessKey, secretKey)
            .build()
    }
}