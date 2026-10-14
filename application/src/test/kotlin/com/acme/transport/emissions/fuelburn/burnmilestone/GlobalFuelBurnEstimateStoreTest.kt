package com.acme.transport.emissions.fuelburn.burnmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalFuelBurnEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalFuelBurnEstimateStore()
        val item = newGlobalFuelBurnEstimate("code-310", "label-945")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
