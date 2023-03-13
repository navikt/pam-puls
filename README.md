## Beskrivelse

pam-puls henter eksport fra Amplitude og tilbyr metrikkene på REST API og Kafka. Applikasjonen filtrerer ut ukjente metrikker og aggregerer metrikker før de lagres i en database.

PS: Alle timestamps er lagret som UTC.

Applikasjonen henter data fra amplitude gjennom [ResearchOps proxy](https://github.com/navikt/reops-proxy).
