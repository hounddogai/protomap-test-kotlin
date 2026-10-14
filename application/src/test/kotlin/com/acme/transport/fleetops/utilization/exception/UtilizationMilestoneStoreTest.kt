package com.acme.transport.fleetops.utilization.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class UtilizationMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryUtilizationMilestoneStore()
        val item = newUtilizationMilestone("code-318", "label-224")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
