package com.acme.transport.safety.incident.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultIncidentCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultIncidentCycleStore()
        val item = newDefaultIncidentCycle("code-245", "name-99")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
