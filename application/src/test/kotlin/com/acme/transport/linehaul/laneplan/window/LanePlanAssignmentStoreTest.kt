package com.acme.transport.linehaul.laneplan.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LanePlanAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLanePlanAssignmentStore()
        val item = newLanePlanAssignment("code-105", "name-173")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
