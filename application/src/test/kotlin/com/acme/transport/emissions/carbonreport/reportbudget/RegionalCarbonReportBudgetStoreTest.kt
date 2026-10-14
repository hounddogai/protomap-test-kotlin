package com.acme.transport.emissions.carbonreport.reportbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalCarbonReportBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalCarbonReportBudgetStore()
        val item = newRegionalCarbonReportBudget("code-161", "label-817")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
