package com.acme.transport.safety.incident.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IncidentLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIncidentLedgerStore()
        val item = newIncidentLedger("code-83", "title-182")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
