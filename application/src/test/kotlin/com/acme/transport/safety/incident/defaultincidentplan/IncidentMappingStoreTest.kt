package com.acme.transport.safety.incident.defaultincidentplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IncidentMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIncidentMappingStore()
        val item = newIncidentMapping("code-452", "label-347")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
