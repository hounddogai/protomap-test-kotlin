package com.acme.transport.emissions.fuelburn.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingFuelBurnThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingFuelBurnThresholdStore()
        val item = newPendingFuelBurnThreshold("code-500", "label-421")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
