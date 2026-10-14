package com.acme.transport.emissions.carbonreport.reportbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CarbonReportTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCarbonReportTierStore()
        val item = newCarbonReportTier("code-661", "label-395")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
