package com.acme.transport.safety.incident.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IncidentEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIncidentEntryStore()
        val item = newIncidentEntry("code-193", "label-941")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
