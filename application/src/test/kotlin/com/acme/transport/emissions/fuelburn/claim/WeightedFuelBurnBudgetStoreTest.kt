package com.acme.transport.emissions.fuelburn.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedFuelBurnBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedFuelBurnBudgetStore()
        val item = newWeightedFuelBurnBudget("code-888", "name-952")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
