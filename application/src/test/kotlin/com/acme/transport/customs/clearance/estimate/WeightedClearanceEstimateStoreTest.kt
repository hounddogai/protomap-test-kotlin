package com.acme.transport.customs.clearance.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedClearanceEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedClearanceEstimateStore()
        val item = newWeightedClearanceEstimate("code-354", "name-290")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
