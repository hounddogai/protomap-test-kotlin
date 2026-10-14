package com.acme.transport.fleetops.utilization.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedUtilizationRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedUtilizationRuleStore()
        val item = newWeightedUtilizationRule("code-366", "name-891")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
