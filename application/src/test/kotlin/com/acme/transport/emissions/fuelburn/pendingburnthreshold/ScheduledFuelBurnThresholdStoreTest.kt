package com.acme.transport.emissions.fuelburn.pendingburnthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledFuelBurnThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledFuelBurnThresholdStore()
        val item = newScheduledFuelBurnThreshold("code-678", "name-356")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
