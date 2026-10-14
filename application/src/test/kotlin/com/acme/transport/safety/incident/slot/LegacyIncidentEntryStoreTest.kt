package com.acme.transport.safety.incident.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyIncidentEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyIncidentEntryStore()
        val item = newLegacyIncidentEntry("code-850", "label-51")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
