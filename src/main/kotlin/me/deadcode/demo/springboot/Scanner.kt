package me.deadcode.demo.springboot

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import me.deadcode.demo.springboot.model.ScanResult
import me.deadcode.demo.springboot.repository.UploadedFileRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Service
class Scanner {
    private val logger: Logger = LoggerFactory.getLogger(Scanner::class.java)
    private val parallelism = 2  // Runtime.getRuntime().availableProcessors()
    private val scanningAttempts = 10
    private val clamdPort = 3310
    private var coroScope: CoroutineScope? = null

    private val workers = Executors.newFixedThreadPool(parallelism)
    private val channel = Channel<ScanningInput>(Channel.UNLIMITED)
    private val isRunning = AtomicBoolean(true)

    @Autowired
    private lateinit var repo: UploadedFileRepository

    fun enqueue(id: String) {
        runBlocking {
            channel.trySend(ScanningInput(id))
        }
    }

    fun startWorkers() {
        isRunning.set(true)
        val scope = CoroutineScope(workers.asCoroutineDispatcher() + SupervisorJob()).also { coroScope = it }
        for(idx in 0 until parallelism) {
            scope.launch { scannerBody(idx) }
        }
    }

    fun stopWorkers() {
        isRunning.set(false)
        coroScope?.cancel("Terminating")
    }

    private suspend fun scannerBody(workerId: Int) {
        logger.info("ScannerWorker $workerId started")
        while(isRunning.get()) {
            if (!pingClamd()) {
                delay(1000)
                continue
            }

            val input = channel.receive()
            val entity = withContext(Dispatchers.IO) {
                repo.findByIdOrNull(input.id)
            }

            if (entity == null){
                logger.error("File $input.id not found")
                continue
            }

            logger.info("Scanner[$workerId] going to scan ${entity.id}")
            try {
                when (val res = scanFile(entity.id)) {
                    is AvScanFailed -> {
                        throw ScanException("ScanInvalid")
                    }

                    is AvScanFinished -> {
                        logger.info("Scanner[$workerId] scan ${entity.id} finished $res")
                        entity.avCheckStatus = if (res.ok) ScanResult.SCANNED_OK.r else ScanResult.SCANNED_FAIL.r
                        entity.avResult = res.status

                        withContext(Dispatchers.IO) {
                            repo.save(entity)
                        }
                    }
                }

            } catch (e: Exception){
                logger.info("Scanner[$workerId] scan ${entity.id} failed: $e", e)
                entity.attempts = input.attempt + 1

                if (input.attempt > scanningAttempts) {
                    entity.avCheckStatus = ScanResult.SCANNED_ERROR.r
                    logger.error("Scanning of file ${input.id} failed - expired")

                } else {
                    val nInput = ScanningInput(input.id, input.attempt + 1)
                    logger.info("Re-Enqueueing file ${input.id} attempt ${nInput.attempt}")

                    coroScope?.launch {
                        delay(1000)
                        channel.send(nInput)
                    }
                }

                withContext(Dispatchers.IO) {
                    repo.save(entity)
                }
            }

        }
    }

    private suspend fun clamdSocket(): Socket {
        val selectorManager = SelectorManager(Dispatchers.IO)
        return aSocket(selectorManager).tcp().connect("127.0.0.1", clamdPort)
    }

    /**
     * https://docs.clamav.net/manual/Usage/Scanning.html#clamd
     */
    private suspend fun pingClamd(): Boolean {
        return try {
            clamdSocket().use { socket ->
                val input = socket.openReadChannel()
                val output = socket.openWriteChannel(autoFlush = true)
                output.writeFully("PING".toByteArray())
                val line = input.readUTF8Line() ?: return false
                line.trim().lowercase() == "pong"
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * https://docs.clamav.net/manual/Usage/Scanning.html#clamd
     * https://github.com/Cisco-Talos/clamav-docker
     */
    private suspend fun scanFile(id: String): AvScanResult {
        clamdSocket().use { socket ->
            val input = socket.openReadChannel()
            val output = socket.openWriteChannel(autoFlush = true)
            val cmd = "SCAN /data/av/tmp/$id"

            output.writeFully(cmd.toByteArray())
            val line = input.readUTF8Line() ?: return AvScanFailed
            val parts = line.split(":")
            if (parts.size != 2){
                logger.error("Scanner returned invalid line: $line")
                return AvScanFailed
            }

            val scanRes = parts[1].trim()
            val success = scanRes.lowercase() == "ok"
            return AvScanFinished(success, scanRes)
        }
    }

    companion object {
        sealed class AvScanResult
        object AvScanFailed: AvScanResult()
        data class AvScanFinished(val ok: Boolean, val status: String): AvScanResult()
        data class ScanningInput(val id: String, val attempt: Int = 0)
        open class ScanException: Exception {
            constructor(message: String, ex: Throwable?): super(message, ex) {}
            constructor(message: String): super(message) {}
            constructor(ex: Throwable): super(ex) {}
            constructor(): super() {}
        }
    }

}