package no.nav.arbeidsplassen.puls.batch

import io.micronaut.data.annotation.Query
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import io.micronaut.data.runtime.config.DataSettings
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import java.sql.Timestamp
import java.time.Instant
import javax.transaction.Transactional

@JdbcRepository(dialect = Dialect.POSTGRES)
abstract class BatchRunRepository(private val connection: Connection): CrudRepository<BatchRun, Long> {

    val insertSQL = """insert into "batch_run" ("name","status", "updated", "total_events", "start_time", "end_time") values (?,?,clock_timestamp(),?,?,?)"""
    val updateSQL = """update "batch_run" set "name"=?, "status"=?, "updated"=clock_timestamp(), "total_events"=?, "start_time"=?, "end_time"=? where "id"=?"""

    @Transactional
    override fun <S : BatchRun> save(entity: S): S {
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

    private fun PreparedStatement.prepareSQL(entity: BatchRun) {
        var index=1
        setString(index, entity.name)
        setString(++index, entity.status.name)
        setInt(++index, entity.totalEvents)
        setTimestamp(++index, entity.startTime.toTimestamp())
        setTimestamp(++index, entity.endTime.toTimestamp())
        if (entity.isNew()) {
            DataSettings.QUERY_LOG.debug("Executing SQL INSERT: $insertSQL")
        }
        else {
            setLong(++index, entity.id!!)
            DataSettings.QUERY_LOG.debug("Executing SQL UPDATE: $updateSQL")
        }
        DataSettings.QUERY_LOG.debug("Query index $index")
    }

    @Transactional
    abstract fun findByName(name:String): BatchRun?

    @Transactional
    abstract fun findByStartTimeGreaterThanEquals(startTime: Instant): List<BatchRun>

    @Transactional
    @Query("SELECT * FROM batch_run b WHERE b.start_time <=:startTime AND b.end_time >=:startTime", nativeQuery = true)
    abstract fun startTimeIntersectInterval(startTime: Instant): BatchRun?



}

fun Instant.toTimestamp() = Timestamp.from(this)
