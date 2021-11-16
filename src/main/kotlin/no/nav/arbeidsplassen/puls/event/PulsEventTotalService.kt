package no.nav.arbeidsplassen.puls.event

import jakarta.inject.Singleton

@Singleton
class PulsEventTotalService(private val repository: PulsEventTotalRepository) {


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
       return repository.save(event).toDTO()
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
