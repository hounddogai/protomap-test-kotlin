package com.acme.transport.emissions.fuelburn.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredFuelBurnPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredFuelBurnPlanStore()
        val item = newTieredFuelBurnPlan("code-615", "title-750")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
