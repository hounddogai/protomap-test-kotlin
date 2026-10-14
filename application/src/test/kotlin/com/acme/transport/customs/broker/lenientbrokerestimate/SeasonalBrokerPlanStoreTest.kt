package com.acme.transport.customs.broker.lenientbrokerestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalBrokerPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalBrokerPlanStore()
        val item = newSeasonalBrokerPlan("code-498", "name-471")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
