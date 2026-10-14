package com.acme.transport.emissions.fuelburn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelBurnBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelBurnBaselineStore()
        val item = newFuelBurnBaseline("code-994", "name-228")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
