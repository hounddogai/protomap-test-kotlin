package com.acme.transport.emissions.fuelburn.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelBurnCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelBurnCheckpointStore()
        val item = newFuelBurnCheckpoint("code-287", "name-705")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
