package com.acme.transport.emissions.fuelburn.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedFuelBurnMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedFuelBurnMappingStore()
        val item = newWeightedFuelBurnMapping("code-250", "label-459")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
