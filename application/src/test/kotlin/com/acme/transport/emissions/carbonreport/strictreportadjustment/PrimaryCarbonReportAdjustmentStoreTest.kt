package com.acme.transport.emissions.carbonreport.strictreportadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryCarbonReportAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryCarbonReportAdjustmentStore()
        val item = newPrimaryCarbonReportAdjustment("code-725", "title-532")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
