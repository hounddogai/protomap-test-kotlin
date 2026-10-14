package com.acme.transport.fleetops.utilization.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingUtilizationEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingUtilizationEstimateStore()
        val item = newRollingUtilizationEstimate("code-559", "name-188")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
