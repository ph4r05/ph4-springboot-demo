import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

plugins {
    id("org.springframework.boot") version "3.0.2"
    id("io.spring.dependency-management") version "1.1.0"
    id("org.liquibase.gradle") version "2.1.1"
    id("io.swagger.core.v3.swagger-gradle-plugin") version "2.2.8"
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.spring") version "1.7.22"
    kotlin("plugin.jpa") version "1.7.22"
}

group = "me.deadcode.demo"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

extra["testcontainersVersion"] = "1.17.6"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    // implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
    // implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    // implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
     implementation("org.springframework.boot:spring-boot-starter-data-rest")
    implementation("org.springframework.boot:spring-boot-starter-freemarker")
    // implementation("org.springframework.boot:spring-boot-starter-integration")
    // implementation("org.springframework.boot:spring-boot-starter-jdbc")
    // implementation("org.springframework.boot:spring-boot-starter-security")
    // implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    // implementation("org.springframework.boot:spring-boot-starter-webflux")
    // implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    // implementation("org.hibernate:hibernate-core:6.1.6.Final")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.apache.kafka:kafka-streams")
    // implementation("org.flywaydb:flyway-core")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.liquibase:liquibase-core")
    // implementation("org.springframework.integration:spring-integration-amqp")
    // implementation("org.springframework.integration:spring-integration-http")
    // implementation("org.springframework.integration:spring-integration-jdbc")
    // implementation("org.springframework.integration:spring-integration-jpa")
    // implementation("org.springframework.integration:spring-integration-kafka")
    // implementation("org.springframework.integration:spring-integration-mongodb")
    // implementation("org.springframework.integration:spring-integration-r2dbc")
    // implementation("org.springframework.integration:spring-integration-redis")
    // implementation("org.springframework.integration:spring-integration-security")
    // implementation("org.springframework.integration:spring-integration-stomp")
    // implementation("org.springframework.integration:spring-integration-webflux")
    // implementation("org.springframework.integration:spring-integration-websocket")
    // implementation("org.springframework.kafka:spring-kafka")
    // implementation("org.springframework.session:spring-session-data-redis")
    // implementation("org.springframework.session:spring-session-jdbc")
    // implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")

    implementation("io.netty:netty-resolver-dns-native-macos")
    implementation("ch.qos.logback:logback-core")
    implementation("ch.qos.logback:logback-classic")
    implementation("commons-fileupload:commons-fileupload:1.5")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.14")
    implementation("io.ktor:ktor-network:2.2.3")
    implementation("io.minio:minio:7.1.0")

    // implementation("io.springfox:springfox-swagger2")
    // implementation("io.springfox:springfox-swagger-ui")
    // implementation("io.swagger:swagger-annotations")

    liquibaseRuntime("org.liquibase:liquibase-core:4.16.1")
    liquibaseRuntime("org.liquibase:liquibase-gradle-plugin:2.1.1")
    liquibaseRuntime("org.liquibase:liquibase-groovy-dsl:3.0.2")
    liquibaseRuntime("org.xerial:sqlite-jdbc:3.39.3.0")
    liquibaseRuntime("org.mariadb.jdbc:mariadb-java-client:3.0.6")
    liquibaseRuntime("mysql:mysql-connector-java:8.0.30")
    liquibaseRuntime("org.postgresql:postgresql")
    liquibaseRuntime("org.glassfish.jaxb:jaxb-runtime:2.3.1")
    liquibaseRuntime("org.yaml:snakeyaml:1.32")
    liquibaseRuntime("info.picocli:picocli:4.6.3")
    liquibaseRuntime("org.liquibase.ext:liquibase-hibernate5:3.8")
    liquibaseRuntime("org.springframework.boot:spring-boot")

    // implementation("org.jetbrains.exposed:exposed-core:0.24.1")
    // implementation("org.jetbrains.exposed:exposed-dao:0.24.1")
    // implementation("org.jetbrains.exposed:exposed-jdbc:0.24.1")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    // runtimeOnly("com.h2database:h2")
    // runtimeOnly("io.r2dbc:r2dbc-h2")
    runtimeOnly("org.postgresql:postgresql")
    // runtimeOnly("org.postgresql:r2dbc-postgresql")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.amqp:spring-rabbit-test")
    testImplementation("org.springframework.integration:spring-integration-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:elasticsearch")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.testcontainers:mongodb")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:r2dbc")
    testImplementation("org.testcontainers:rabbitmq")
}

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.Embeddable")
    annotation("jakarta.persistence.MappedSuperclass")
}

liquibase {
    val props = Properties()
    val propertiesFile = File("$rootDir/liquibase.properties")
    propertiesFile.inputStream().use {
        props.load(it)
    }

    project.extra.properties["run_list"]?.let {
        logger.debug("Using run_list from -Prun_list: $it")
        props.put("run_list", it)
    }

    activities.register("main") {
        // val db_url by project.extra.properties
        // val db_user by project.extra.properties
        // val db_password by project.extra.properties
        this.arguments = mapOf(
            "logLevel" to "info",
            "searchPath" to "src/main/resources",
            "changeLogFile" to "src/main/resources/db/changelog/db.changelog-master.yaml",
            "url" to props.getProperty("url"),
            "username" to props.getProperty("username"),
            "password" to props.getProperty("password"),
        )
    }

    // Dev-purposes, integration tests
    activities.register("devt") {
        this.arguments = mapOf(
            "logLevel" to "info",
            "searchPath" to "src/main/resources",
            "changeLogFile" to "src/main/resources/db/changelog/db.changelog-master.yaml",
            "url" to (props.getProperty("dev_url") ?: System.getenv("DB_URL") ?: ("jdbc:postgresql://" + (System.getenv("DB_HOST") ?: "localhost")+ "/demo_db")),
            "username" to (props.getProperty("username") ?: System.getenv("DB_USER") ?: "root"),
            "password" to (props.getProperty("password") ?: System.getenv("DB_PASS") ?: "root"),
        )
    }

    // runList = project.ext.runList
    runList = props["run_list"] ?: "main"

//    activities {
//        sqlite {
//            changeLogFile 'server/src/main/db/migration/db.changelog-master-sqlite.yaml'
//            url properties.get('sqliteurl') ?: 'jdbc:sqlite:exampledb.sqlite'
//        }
//}
}
