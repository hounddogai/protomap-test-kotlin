package com.acme.transport.freightpay.audit.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AuditCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAuditCheckpointStore()
        val item = newAuditCheckpoint("code-462", "name-444")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
