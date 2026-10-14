package com.acme.transport.emissions.fuelburn.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelBurnVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelBurnVarianceStore()
        val item = newFuelBurnVariance("code-490", "title-827")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
