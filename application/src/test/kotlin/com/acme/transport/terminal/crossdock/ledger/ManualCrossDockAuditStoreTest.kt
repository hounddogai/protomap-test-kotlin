package com.acme.transport.terminal.crossdock.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualCrossDockAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualCrossDockAuditStore()
        val item = newManualCrossDockAudit("code-826", "name-448")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
