package com.acme.transport.emissions.fuelburn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelBurnOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelBurnOverrideStore()
        val item = newFuelBurnOverride("code-571", "name-741")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
