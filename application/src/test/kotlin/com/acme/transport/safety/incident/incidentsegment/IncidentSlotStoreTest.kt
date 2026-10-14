package com.acme.transport.safety.incident.incidentsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IncidentSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIncidentSlotStore()
        val item = newIncidentSlot("code-637", "title-279")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
