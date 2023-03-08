plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("kapt") version "1.7.10"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.7.10"
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("io.micronaut.application") version "3.7.4"
}

version = "0.1"
group = "no.nav.arbeidsplassen.puls"

val kotlinVersion = project.properties["kotlinVersion"]
val micronautKafkaVersion = project.properties["micronautKafkaVersion"]
val micronautMicrometerVersion = project.properties["micronautMicrometerVersion"]
val logbackEncoderVersion = project.properties["logbackEncoderVersion"]
val jakartaPersistenceVersion = project.properties["jakartaPersistenceVersion"]
val postgresqlVersion = project.properties["postgresqlVersion"]
val tcVersion = project.properties["tcVersion"]
val javaVersion = project.properties["javaVersion"]
val jacksonVersion = project.properties["jacksonVersion"]
val jupiterVersion = project.properties["jupiterVersion"]

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jcenter.bintray.com")
}

micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("no.nav.arbeidsplassen.puls.*")
    }
}

dependencies {
    kapt("io.micronaut:micronaut-http-validation")
    kapt("io.micronaut.data:micronaut-data-processor")
    implementation("io.micronaut:micronaut-http-client")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("javax.annotation:javax.annotation-api")
    implementation ("jakarta.persistence:jakarta.persistence-api:${jakartaPersistenceVersion}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    implementation("io.micronaut.kafka:micronaut-kafka:${micronautKafkaVersion}")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:${logbackEncoderVersion}")
    implementation("io.micronaut:micronaut-validation")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin:${jacksonVersion}")
    implementation("io.micronaut.micrometer:micronaut-micrometer-core")
    implementation("io.micronaut.micrometer:micronaut-micrometer-registry-prometheus")
    implementation("io.micronaut:micronaut-management")
    implementation("io.micronaut.data:micronaut-data-jdbc")
    implementation("org.postgresql:postgresql:${postgresqlVersion}")
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")
    implementation("io.micronaut.flyway:micronaut-flyway")
    testImplementation("io.micronaut.test:micronaut-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${jupiterVersion}")
    testImplementation("org.testcontainers:postgresql:${tcVersion}")
}

application {
    mainClass.set("no.nav.arbeidsplassen.puls.Application")
}
java {
    sourceCompatibility = JavaVersion.toVersion("$javaVersion")
}


tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "$javaVersion"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "$javaVersion"
        }
    }
    test {
        exclude("**/*IT.class")
    }
}
