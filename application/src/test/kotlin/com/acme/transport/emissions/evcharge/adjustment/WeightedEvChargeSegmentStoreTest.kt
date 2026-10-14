package com.acme.transport.emissions.evcharge.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedEvChargeSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedEvChargeSegmentStore()
        val item = newWeightedEvChargeSegment("code-396", "label-47")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
