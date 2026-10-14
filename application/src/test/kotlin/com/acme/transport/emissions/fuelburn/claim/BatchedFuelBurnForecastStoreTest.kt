package com.acme.transport.emissions.fuelburn.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedFuelBurnForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedFuelBurnForecastStore()
        val item = newBatchedFuelBurnForecast("code-952", "label-866")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
