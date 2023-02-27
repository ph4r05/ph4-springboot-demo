package me.deadcode.demo.springboot

import me.deadcode.demo.springboot.repository.UploadedFileRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener


@SpringBootApplication
class HelloWorldApplication {
    private val logger: Logger = LoggerFactory.getLogger(HelloWorldApplication::class.java)

    @Autowired
    private lateinit var repo: UploadedFileRepository

    @Autowired
    private lateinit var scanner: Scanner

    @EventListener(ApplicationReadyEvent::class)
    fun runAfterStartup() {
        logger.info("Application started")
        logger.info("Scanner: $scanner")
        logger.info("Repo: $repo")

        scanner.startWorkers()
    }
}

fun main(args: Array<String>) {
    runApplication<HelloWorldApplication>(*args)
}
