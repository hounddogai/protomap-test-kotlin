package com.acme.transport.fleetops.utilization.utilizationplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveUtilizationPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveUtilizationPolicyStore()
        val item = newAdaptiveUtilizationPolicy("code-45", "label-328")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
