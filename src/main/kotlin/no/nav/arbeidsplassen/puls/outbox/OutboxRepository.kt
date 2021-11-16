package no.nav.arbeidsplassen.puls.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import io.micronaut.data.runtime.config.DataSettings
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import java.time.Instant
import javax.transaction.Transactional

@JdbcRepository(dialect = Dialect.POSTGRES)
abstract class OutboxRepository(private val connection: Connection, private val objectMapper: ObjectMapper): CrudRepository<Outbox, Long> {

    val insertSQL = """insert into "outbox" ("oid", "type", "status", "payload", "updated" ) values (?,?,?,?::jsonb,clock_timestamp())"""
    val updateSQL = """update "outbox" set "oid"=?, "type"=?, "status"=?, "payload"=?::jsonb, "updated"=clock_timestamp() where "id"=?"""

    @Transactional
    override fun <S : Outbox> save(entity: S): S {
        if (entity.isNew()) {
            connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS).apply {
                prepareSQL(entity)
                execute()
                check(generatedKeys.next())
                @Suppress("UNCHECKED_CAST")
                return entity.copy(id = generatedKeys.getLong(1)) as S
            }
        }
        else {
            connection.prepareStatement(updateSQL).apply {
                prepareSQL(entity)
                check(executeUpdate() == 1 )
                return entity
            }
        }
    }

    private fun PreparedStatement.prepareSQL(entity: Outbox) {
        var index=1
        setString(index, entity.oid)
        setString(++index, entity.type.name)
        setString(++index, entity.status.name)
        setString(++index, objectMapper.writeValueAsString(entity.payload))
        if (entity.isNew()) {
            DataSettings.QUERY_LOG.debug("Executing SQL INSERT: $insertSQL")
        }
        else {
            setLong(++index, entity.id!!)
            DataSettings.QUERY_LOG.debug("Executing SQL UPDATE: $updateSQL")
        }
    }

    @Transactional
    abstract fun findByStatusOrderByUpdated(outboxStatus: OutboxStatus, pageable: Pageable): List<Outbox>

    @Transactional
    abstract fun deleteByStatusAndUpdatedBefore(outboxStatus: OutboxStatus, before: Instant): Int

}
