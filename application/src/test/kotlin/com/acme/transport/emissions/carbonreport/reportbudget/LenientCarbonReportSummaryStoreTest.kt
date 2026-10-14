package com.acme.transport.emissions.carbonreport.reportbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientCarbonReportSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientCarbonReportSummaryStore()
        val item = newLenientCarbonReportSummary("code-530", "name-316")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
