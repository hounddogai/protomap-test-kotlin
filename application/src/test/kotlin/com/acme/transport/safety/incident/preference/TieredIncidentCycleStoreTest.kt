package com.acme.transport.safety.incident.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredIncidentCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredIncidentCycleStore()
        val item = newTieredIncidentCycle("code-766", "name-832")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
