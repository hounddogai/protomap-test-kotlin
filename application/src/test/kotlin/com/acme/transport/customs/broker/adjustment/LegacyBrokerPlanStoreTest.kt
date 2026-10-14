package com.acme.transport.customs.broker.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyBrokerPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyBrokerPlanStore()
        val item = newLegacyBrokerPlan("code-876", "name-942")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
