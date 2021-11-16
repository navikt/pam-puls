package no.nav.arbeidsplassen.puls.event

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import io.micronaut.data.runtime.config.DataSettings
import no.nav.arbeidsplassen.puls.batch.toTimestamp
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import java.time.ZoneOffset
import javax.transaction.Transactional

@JdbcRepository(dialect = Dialect.POSTGRES)
abstract class PulsEventTotalRepository(private val connection: Connection, private val objectMapper: ObjectMapper): CrudRepository<PulsEventTotal, Long> {

    val insertSQL = """insert into "puls_event_total" ("oid", "total", "type", "properties", "created", "updated" ) values (?,?,?,?::jsonb,?,clock_timestamp())"""
    val updateSQL = """update "puls_event_total" set "oid"=?, "total"=?, "type"=?, "properties"=?::jsonb, "created"=?, "updated"=clock_timestamp() where "id"=?"""


    @Transactional
    abstract fun findByOidAndType(oid:String, type: String): PulsEventTotal?

    @Transactional
    override fun <S : PulsEventTotal> save(entity: S): S {
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

    private fun PreparedStatement.prepareSQL(entity: PulsEventTotal) {
        setObject(1, entity.oid)
        setLong(2, entity.total)
        setString(3, entity.type)
        setString(4, objectMapper.writeValueAsString(entity.properties))
        setTimestamp(5, entity.created.toTimestamp())
        if (entity.isNew()) {
            DataSettings.QUERY_LOG.debug("Executing SQL INSERT: $insertSQL")
        }
        else {
            setLong(6, entity.id!!)
            DataSettings.QUERY_LOG.debug("Executing SQL UPDATE: $updateSQL")
        }
    }
    @Transactional
    abstract fun findByOid(oid: String): List<PulsEventTotal>
}
