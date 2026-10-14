package com.acme.transport.safety.incident.incidentsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IncidentRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIncidentRequestStore()
        val item = newIncidentRequest("code-654", "title-81")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
