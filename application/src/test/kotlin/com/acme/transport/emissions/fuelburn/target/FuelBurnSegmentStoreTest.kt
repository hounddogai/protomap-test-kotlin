package com.acme.transport.emissions.fuelburn.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelBurnSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelBurnSegmentStore()
        val item = newFuelBurnSegment("code-472", "label-864")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
