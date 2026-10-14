package com.acme.transport.safety.incident.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyIncidentRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyIncidentRequestStore()
        val item = newLegacyIncidentRequest("code-432", "title-775")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
