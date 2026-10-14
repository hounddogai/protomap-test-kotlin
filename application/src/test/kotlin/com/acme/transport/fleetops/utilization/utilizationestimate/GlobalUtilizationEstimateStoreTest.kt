package com.acme.transport.fleetops.utilization.utilizationestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalUtilizationEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalUtilizationEstimateStore()
        val item = newGlobalUtilizationEstimate("code-277", "title-773")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
