package com.acme.transport.fleetops.utilization.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledUtilizationMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledUtilizationMilestoneStore()
        val item = newScheduledUtilizationMilestone("code-997", "name-630")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
