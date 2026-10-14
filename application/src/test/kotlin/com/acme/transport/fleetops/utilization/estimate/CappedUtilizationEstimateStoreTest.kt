package com.acme.transport.fleetops.utilization.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedUtilizationEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedUtilizationEstimateStore()
        val item = newCappedUtilizationEstimate("code-992", "label-945")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
