package com.acme.transport.emissions.fuelburn.burnthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledFuelBurnCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledFuelBurnCycleStore()
        val item = newScheduledFuelBurnCycle("code-101", "name-697")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
