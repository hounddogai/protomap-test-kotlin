package com.acme.transport.fleet

import com.acme.transport.platform.JournalEntry
import com.acme.transport.platform.OperationJournal
import com.google.protobuf.Message

/** The fleet audit port keeps accepted operations for support and replay exports. */
class FleetRepository(private val journal: OperationJournal) {
    suspend fun record(tenant: String, operation: String, message: Message) {
        journal.append(JournalEntry(tenant, "fleet", operation, message.toByteArray()))
    }
}
