package com.acme.transport.emissions.fuelburn.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalFuelBurnThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalFuelBurnThresholdStore()
        val item = newRegionalFuelBurnThreshold("code-257", "label-979")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
