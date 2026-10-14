package com.acme.transport.fleetops.utilization.utilizationplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualUtilizationPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualUtilizationPlanStore()
        val item = newManualUtilizationPlan("code-757", "name-490")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
