package no.nav.arbeidsplassen.puls
import io.micronaut.runtime.Micronaut

object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
            .packages("no.nav.arbeidsplassen.puls")
            .mainClass(Application.javaClass)
            .start()
    }

}

