package me.deadcode.demo.springboot.controller

import io.minio.MinioClient
import io.minio.UploadObjectArgs
import me.deadcode.demo.springboot.Scanner
import me.deadcode.demo.springboot.model.CheckResult
import me.deadcode.demo.springboot.model.ScanResult
import me.deadcode.demo.springboot.model.UploadFileResult
import me.deadcode.demo.springboot.entity.UploadedFile
import me.deadcode.demo.springboot.repository.UploadedFileRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.data.repository.findByIdOrNull
import org.springframework.hateoas.MediaTypes
import org.springframework.http.*
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.*
import kotlin.io.path.deleteIfExists


@CrossOrigin(maxAge = 3600)
@RestController
class AppsController {
    private val basePath = "data"
    private val logger: Logger = LoggerFactory.getLogger(AppsController::class.java)

    @Autowired
    private lateinit var repo: UploadedFileRepository

    @Autowired
    private lateinit var scanner: Scanner

    @Autowired
    private lateinit var minioClient: MinioClient

    @Value("\${minio.bucket.name}")
    private val bucket: String? = null

    @GetMapping("/check/{uuid:.+}", produces = [MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun check(@PathVariable uuid: String): ResponseEntity<CheckResult> {
        val entity = getFileEntity(uuid) ?: return ResponseEntity.notFound().build()
        return when (ScanResult.fromInt(entity.avCheckStatus)) {
            ScanResult.SCANNED_OK -> {
                ResponseEntity.ok(CheckResult(entity.id, entity.avCheckStatus, "File was accepted"))
            }
            ScanResult.SCANNED_FAIL -> {
                ResponseEntity.status(422).body(CheckResult(entity.id, entity.avCheckStatus, "File was not accepted due to failed AV check: ${entity.avResult}"))
            }
            ScanResult.SCANNED_ERROR -> {
                ResponseEntity.status(422).body(CheckResult(entity.id, entity.avCheckStatus, "File could not be scanned"))
            }
            else -> {
                ResponseEntity.ok(CheckResult(entity.id, entity.avCheckStatus, "File is being scanned"))
            }
        }
    }

    @GetMapping("/download/{uuid:.+}")
    fun download(@PathVariable uuid: String): ResponseEntity<Any> {
        val entity = getFileEntity(uuid) ?: return ResponseEntity.notFound().build()
        if (entity.avCheckStatus == 0) {
            return ResponseEntity.status(HttpStatusCode.valueOf(410)).build()
        }

        val path = getFilePath(entity.id)
        val resource: Resource?
        try {
            resource = InputStreamResource(FileInputStream(path.toFile()), entity.name)
        } catch (e: MalformedURLException) {
            logger.error("Error in file download: $e", e)
            return ResponseEntity.internalServerError().build()
        }

        logger.info("file being downloaded ${entity.id}, ${entity.name}")
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
            .body(resource)
    }

    @PostMapping(
        "/upload",
        produces = [MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun upload(@RequestParam("file") file: MultipartFile): ResponseEntity<Any> {
        val id = UUID.randomUUID().toString()
        val fileName = StringUtils.cleanPath(file.originalFilename ?: file.name)

        val path = getFilePath(id)
        val entity = UploadedFile(id, fileName, path.toString(), avCheckStatus = -1)
        try {
            logger.info("Going to upload file $path")
            doUpload(entity, file.inputStream, path)

            repo.save(entity)
            scanner.enqueue(entity.id)

        } catch (e: IOException) {
            logger.error("Error in file upload: $e", e)
            path.deleteIfExists()
            return ResponseEntity.ok(UploadFileResult(success = 0, errorCode = 1, error = "File upload error"))
        }

        return ResponseEntity.ok(UploadFileResult(success = 1, uuid = id))
    }

    private fun getFileEntity(uuid: String): UploadedFile? {
        val cleanUuid = StringUtils.cleanPath(uuid)
        return repo.findByIdOrNull(cleanUuid)
    }

    private fun getFilePath(uuid: String): Path {
        return Paths.get(basePath, uuid)
    }

    private fun doUpload(entity: UploadedFile, dis: InputStream, path: Path){
        val buffer = ByteArray(1024*1024*5)
        val hasher = MessageDigest.getInstance("SHA-256")
        val doRead = { dis.read(buffer) }

        FileOutputStream(path.toFile()).use { fos ->
            var bytesRead = doRead()
            while (bytesRead != -1) {
                hasher.update(buffer, 0, bytesRead)
                fos.write(buffer, 0, bytesRead)
                bytesRead = doRead()
            }
        }

        val digest = hasher.digest()
        entity.checksum = digest.fold("") { str, x -> str + "%02x".format(x) }

        // uploadMiniIo(entity)
    }

    private fun uploadMiniIo(entity: UploadedFile){
        try {
            minioClient.uploadObject(
                UploadObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(entity.id)
                    .filename(entity.path)
                    .build()
            )
        } catch (e: Exception) {
            logger.error("Could not upload to MinIO: $e", e)
        }
    }

    @Transactional
    fun storeEntity(file: UploadedFile) {
        repo.save(file)
    }
}
