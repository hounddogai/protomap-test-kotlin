package com.acme.transport.safety.incident.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredIncidentBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredIncidentBaselineStore()
        val item = newTieredIncidentBaseline("code-763", "name-806")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
