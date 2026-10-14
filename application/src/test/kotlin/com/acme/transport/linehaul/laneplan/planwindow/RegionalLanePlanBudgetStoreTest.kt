package com.acme.transport.linehaul.laneplan.planwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalLanePlanBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalLanePlanBudgetStore()
        val item = newRegionalLanePlanBudget("code-84", "label-774")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
