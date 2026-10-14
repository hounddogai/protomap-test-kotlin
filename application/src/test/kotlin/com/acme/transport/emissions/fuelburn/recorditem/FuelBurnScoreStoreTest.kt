package com.acme.transport.emissions.fuelburn.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelBurnScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelBurnScoreStore()
        val item = newFuelBurnScore("code-754", "label-355")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
