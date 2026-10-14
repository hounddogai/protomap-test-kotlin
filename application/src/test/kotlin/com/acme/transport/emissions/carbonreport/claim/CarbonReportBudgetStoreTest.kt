package com.acme.transport.emissions.carbonreport.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CarbonReportBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCarbonReportBudgetStore()
        val item = newCarbonReportBudget("code-64", "name-849")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
