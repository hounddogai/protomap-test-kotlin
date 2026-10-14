package com.acme.transport.emissions.fuelburn.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedFuelBurnWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedFuelBurnWindowStore()
        val item = newWeightedFuelBurnWindow("code-463", "name-771")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
