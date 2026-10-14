package com.acme.transport.safety.incident.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalIncidentRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalIncidentRequestStore()
        val item = newGlobalIncidentRequest("code-318", "title-15")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
