package me.deadcode.demo.springboot.repository

import me.deadcode.demo.springboot.entity.UploadedFile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UploadedFileRepository : JpaRepository<UploadedFile, String>
