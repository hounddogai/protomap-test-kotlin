package com.acme.transport.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.sql.DataSource

/** Database records end at this adapter; no RPC request is reconstructed from the journal. */
data class JournalEntry(val tenant: String, val domain: String, val operation: String, val payload: ByteArray)

interface OperationJournal {
    suspend fun append(entry: JournalEntry)
}

class JdbcOperationJournal(private val dataSource: DataSource) : OperationJournal {
    override suspend fun append(entry: JournalEntry): Unit = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                connection.prepareStatement(
                    "INSERT INTO transport_journal (tenant_id, domain_name, operation_name, payload) VALUES (?, ?, ?, ?)"
                ).use { statement ->
                    statement.setString(1, entry.tenant)
                    statement.setString(2, entry.domain)
                    statement.setString(3, entry.operation)
                    statement.setBytes(4, entry.payload)
                    statement.executeUpdate()
                }
                connection.commit()
            } catch (error: Exception) {
                connection.rollback()
                throw error
            }
        }
    }
}
