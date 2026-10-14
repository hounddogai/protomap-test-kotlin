package com.acme.transport.emissions.fuelburn.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelBurnCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelBurnCycleStore()
        val item = newFuelBurnCycle("code-535", "name-510")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
