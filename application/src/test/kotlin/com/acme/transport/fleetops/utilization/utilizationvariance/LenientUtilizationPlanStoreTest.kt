package com.acme.transport.fleetops.utilization.utilizationvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientUtilizationPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientUtilizationPlanStore()
        val item = newLenientUtilizationPlan("code-157", "title-210")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
