package com.acme.transport.customs.broker.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedBrokerPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedBrokerPlanStore()
        val item = newWeightedBrokerPlan("code-452", "name-399")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
