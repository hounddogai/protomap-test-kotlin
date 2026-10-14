package com.acme.transport.emissions.carbonreport.reportadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalCarbonReportAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalCarbonReportAdjustmentStore()
        val item = newRegionalCarbonReportAdjustment("code-529", "name-122")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
