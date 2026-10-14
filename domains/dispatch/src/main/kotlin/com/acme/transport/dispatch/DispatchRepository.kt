package com.acme.transport.dispatch

import com.acme.transport.platform.JournalEntry
import com.acme.transport.platform.OperationJournal
import com.google.protobuf.Message

/** The dispatch audit port keeps accepted operations for support and replay exports. */
class DispatchRepository(private val journal: OperationJournal) {
    suspend fun record(tenant: String, operation: String, message: Message) {
        journal.append(JournalEntry(tenant, "dispatch", operation, message.toByteArray()))
    }
}
