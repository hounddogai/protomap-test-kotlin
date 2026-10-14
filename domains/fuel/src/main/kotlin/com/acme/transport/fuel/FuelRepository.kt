package com.acme.transport.fuel

import com.acme.transport.platform.JournalEntry
import com.acme.transport.platform.OperationJournal
import com.google.protobuf.Message

/** The fuel audit port keeps accepted operations for support and replay exports. */
class FuelRepository(private val journal: OperationJournal) {
    suspend fun record(tenant: String, operation: String, message: Message) {
        journal.append(JournalEntry(tenant, "fuel", operation, message.toByteArray()))
    }
}
