package me.deadcode.demo.springboot.entity

import jakarta.persistence.*
import me.deadcode.demo.springboot.model.ScanResult
import java.util.*


@Entity
@Table(name = "uploaded_file")
data class UploadedFile(
    @Id
    var id: String = UUID.randomUUID().toString(),
    @Column(nullable = false)
    var name: String? = null,
    @Column(nullable = false)
    var path: String? = null,
    @Column(nullable = false)
    var avCheckStatus: Int = ScanResult.WAITING.r,
    @Column(nullable = false)
    var checksum: String? = null,
    @Column(nullable = false)
    var attempts: Int = 0,
    @Column(nullable = true)
    var avResult: String? = null,
)
