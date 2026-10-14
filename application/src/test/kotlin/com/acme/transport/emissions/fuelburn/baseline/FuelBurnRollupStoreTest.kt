package com.acme.transport.emissions.fuelburn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelBurnRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelBurnRollupStore()
        val item = newFuelBurnRollup("code-349", "name-500")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
