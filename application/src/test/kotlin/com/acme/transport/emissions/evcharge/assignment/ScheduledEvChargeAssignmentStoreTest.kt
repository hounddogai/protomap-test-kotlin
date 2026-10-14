package com.acme.transport.emissions.evcharge.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledEvChargeAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledEvChargeAssignmentStore()
        val item = newScheduledEvChargeAssignment("code-157", "label-516")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
