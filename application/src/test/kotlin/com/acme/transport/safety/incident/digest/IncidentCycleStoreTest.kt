package com.acme.transport.safety.incident.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IncidentCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIncidentCycleStore()
        val item = newIncidentCycle("code-597", "label-360")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
