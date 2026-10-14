package com.acme.transport.safety.incident.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IncidentAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIncidentAssignmentStore()
        val item = newIncidentAssignment("code-402", "title-205")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
