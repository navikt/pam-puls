plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    id("org.jetbrains.kotlin.kapt") version "1.5.31"
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("io.micronaut.application") version "2.0.6"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.5.31"
}

version = "0.1"
group "no.nav.arbeidsplassen.puls"

val kotlinVersion=project.properties.get("kotlinVersion")
val micronautKafkaVersion=project.properties.get("micronautKafkaVersion")
val micronautMicrometerVersion=project.properties.get("micronautMicrometerVersion")
val logbackEncoderVersion=project.properties.get("logbackEncoderVersion")

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jcenter.bintray.com")
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
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
    implementation("io.micronaut:micronaut-http-client")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("javax.annotation:javax.annotation-api")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    implementation("io.micronaut.kafka:micronaut-kafka:${micronautKafkaVersion}")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:${logbackEncoderVersion}")
    implementation("io.micronaut:micronaut-validation")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.micronaut.micrometer:micronaut-micrometer-core")
    implementation("io.micronaut.micrometer:micronaut-micrometer-registry-prometheus")
    implementation("io.micronaut:micronaut-management")
    testImplementation("io.micronaut.test:micronaut-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")

}


application {
    mainClass.set("no.nav.arbeidsplassen.puls.Application")
}
java {
    sourceCompatibility = JavaVersion.toVersion("11")
}


tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    test {
        exclude("**/*IT.class")
    }

}
