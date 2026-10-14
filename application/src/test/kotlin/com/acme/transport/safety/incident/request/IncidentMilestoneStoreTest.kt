package com.acme.transport.safety.incident.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IncidentMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIncidentMilestoneStore()
        val item = newIncidentMilestone("code-287", "name-814")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
