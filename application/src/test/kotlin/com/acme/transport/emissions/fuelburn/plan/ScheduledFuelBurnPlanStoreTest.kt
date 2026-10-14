package com.acme.transport.emissions.fuelburn.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledFuelBurnPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledFuelBurnPlanStore()
        val item = newScheduledFuelBurnPlan("code-722", "label-835")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
