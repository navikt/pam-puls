package no.nav.arbeidsplassen.puls

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Value
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.time.LocalDateTime
import jakarta.inject.Singleton
import reactor.core.publisher.Mono

@Singleton
class LeaderElection(@Client("LeaderElect") val client: HttpClient,
                     @Value("\${ELECTOR_PATH:NOLEADERELECTION}") val electorPath: String,
                     val objectMapper: ObjectMapper) {

    private val hostname = InetAddress.getLocalHost().hostName
    private var leader =  "";
    private var lastCalled = LocalDateTime.MIN
    private val electorUri = "http://"+electorPath;

    init {
        LOG.info("leader elaction initialized this hostname is $hostname")
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(LeaderElection::class.java)
    }

    fun isLeader(): Boolean {
        return hostname == getLeader();
    }

    private fun getLeader(): String {
        if (electorPath == "NOLEADERELECTION") return hostname;
        if (leader.isBlank() || lastCalled.isBefore(LocalDateTime.now().minusMinutes(2))) {
            leader = objectMapper.readValue(Mono.from(client.retrieve(electorUri)).block(), Elector::class.java).name
            LOG.info("Running leader election getLeader is {} ", leader)
            lastCalled = LocalDateTime.now()
        }
        return leader
    }
}

data class Elector(val name: String)
