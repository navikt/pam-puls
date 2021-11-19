package no.nav.arbeidsplassen.puls.event

import jakarta.inject.Singleton
import no.nav.arbeidsplassen.puls.outbox.Outbox
import no.nav.arbeidsplassen.puls.outbox.OutboxRepository

@Singleton
class PulsEventTotalService(private val repository: PulsEventTotalRepository, private val outboxRepository: OutboxRepository) {


    fun findByOidAndType(oid: String, type: String): PulsEventTotalDTO? {
        return repository.findByOidAndType(oid, type)?.toDTO()
    }

    fun updatePulsEventTotal(dtos: List<PulsEventTotalDTO>): List<PulsEventTotalDTO> {
        return dtos.map { updatePulsEventTotal(it) }
    }

    fun updatePulsEventTotal(dto: PulsEventTotalDTO): PulsEventTotalDTO {
        val event = repository.findByOidAndType(dto.oid, dto.type)?.let {
            it.copy(total = it.total + dto.total, properties = dto.properties)
        } ?: dto.toEntity()
        val saved = repository.save(event).toDTO()
        outboxRepository.save(Outbox(oid = saved.oid, payload = saved))
        return saved
    }

    private fun PulsEventTotal.toDTO(): PulsEventTotalDTO {
        return PulsEventTotalDTO(id = id, oid = oid, total = total, type=type, properties = properties, created=created, updated=updated)
    }

    private fun PulsEventTotalDTO.toEntity(): PulsEventTotal {
        return PulsEventTotal(id=id, oid=oid, total=total, type=type, properties = properties, created=created, updated=updated)
    }

    fun findByOid(oid: String): List<PulsEventTotalDTO> {
        return repository.findByOid(oid).map { it.toDTO() }
    }

}

