package com.acme.transport.fleetops.utilization.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class UtilizationPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryUtilizationPlanStore()
        val item = newUtilizationPlan("code-482", "title-861")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
