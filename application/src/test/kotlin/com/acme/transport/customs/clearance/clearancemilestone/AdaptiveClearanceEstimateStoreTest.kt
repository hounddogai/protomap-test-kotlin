package com.acme.transport.customs.clearance.clearancemilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveClearanceEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveClearanceEstimateStore()
        val item = newAdaptiveClearanceEstimate("code-926", "label-703")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
