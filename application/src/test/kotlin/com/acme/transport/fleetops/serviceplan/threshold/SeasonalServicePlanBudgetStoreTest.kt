package com.acme.transport.fleetops.serviceplan.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalServicePlanBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalServicePlanBudgetStore()
        val item = newSeasonalServicePlanBudget("code-983", "name-49")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
