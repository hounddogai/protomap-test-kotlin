package com.acme.transport.emissions.fuelburn.burnrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedFuelBurnCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedFuelBurnCycleStore()
        val item = newBatchedFuelBurnCycle("code-15", "title-889")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
