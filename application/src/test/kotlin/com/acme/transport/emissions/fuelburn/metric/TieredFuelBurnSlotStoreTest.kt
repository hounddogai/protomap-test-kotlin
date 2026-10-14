package com.acme.transport.emissions.fuelburn.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredFuelBurnSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredFuelBurnSlotStore()
        val item = newTieredFuelBurnSlot("code-481", "name-136")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
