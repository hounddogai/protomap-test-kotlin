package com.acme.transport.emissions.fuelburn.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelBurnTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelBurnTierStore()
        val item = newFuelBurnTier("code-477", "label-133")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
