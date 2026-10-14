package com.acme.transport.emissions.fuelburn.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredFuelBurnRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredFuelBurnRollupStore()
        val item = newTieredFuelBurnRollup("code-261", "label-807")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
