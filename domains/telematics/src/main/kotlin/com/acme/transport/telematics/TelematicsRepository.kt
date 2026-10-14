package com.acme.transport.telematics

import com.acme.transport.platform.JournalEntry
import com.acme.transport.platform.OperationJournal
import com.google.protobuf.Message

/** The telematics audit port keeps accepted operations for support and replay exports. */
class TelematicsRepository(private val journal: OperationJournal) {
    suspend fun record(tenant: String, operation: String, message: Message) {
        journal.append(JournalEntry(tenant, "telematics", operation, message.toByteArray()))
    }
}
