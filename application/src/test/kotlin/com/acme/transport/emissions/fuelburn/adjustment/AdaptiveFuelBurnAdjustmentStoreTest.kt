package com.acme.transport.emissions.fuelburn.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveFuelBurnAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveFuelBurnAdjustmentStore()
        val item = newAdaptiveFuelBurnAdjustment("code-237", "label-529")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
