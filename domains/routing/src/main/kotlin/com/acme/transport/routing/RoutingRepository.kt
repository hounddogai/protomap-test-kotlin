package com.acme.transport.routing

import com.acme.transport.platform.JournalEntry
import com.acme.transport.platform.OperationJournal
import com.google.protobuf.Message

/** The routing audit port keeps accepted operations for support and replay exports. */
class RoutingRepository(private val journal: OperationJournal) {
    suspend fun record(tenant: String, operation: String, message: Message) {
        journal.append(JournalEntry(tenant, "routing", operation, message.toByteArray()))
    }
}
