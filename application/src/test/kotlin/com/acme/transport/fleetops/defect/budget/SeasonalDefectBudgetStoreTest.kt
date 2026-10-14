package com.acme.transport.fleetops.defect.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalDefectBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalDefectBudgetStore()
        val item = newSeasonalDefectBudget("code-380", "title-806")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
