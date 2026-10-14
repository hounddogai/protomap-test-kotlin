package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackServicePlanRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackServicePlanRollupStore()
        val item = newFallbackServicePlanRollup("code-232", "label-883")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
