package com.acme.transport.emissions.carbonreport.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedCarbonReportRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedCarbonReportRecordStore()
        val item = newWeightedCarbonReportRecord("code-145", "label-404")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
