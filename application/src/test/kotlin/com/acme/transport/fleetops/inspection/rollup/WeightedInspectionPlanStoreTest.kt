package com.acme.transport.fleetops.inspection.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedInspectionPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedInspectionPlanStore()
        val item = newWeightedInspectionPlan("code-900", "name-158")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
