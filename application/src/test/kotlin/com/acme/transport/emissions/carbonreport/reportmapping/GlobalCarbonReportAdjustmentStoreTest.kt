package com.acme.transport.emissions.carbonreport.reportmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalCarbonReportAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalCarbonReportAdjustmentStore()
        val item = newGlobalCarbonReportAdjustment("code-211", "name-615")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
