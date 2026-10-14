package com.acme.transport.safety.incident.incidentsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingIncidentRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingIncidentRequestStore()
        val item = newPendingIncidentRequest("code-351", "title-42")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
