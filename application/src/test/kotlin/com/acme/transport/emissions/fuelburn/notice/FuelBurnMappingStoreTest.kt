package com.acme.transport.emissions.fuelburn.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelBurnMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelBurnMappingStore()
        val item = newFuelBurnMapping("code-234", "title-853")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
