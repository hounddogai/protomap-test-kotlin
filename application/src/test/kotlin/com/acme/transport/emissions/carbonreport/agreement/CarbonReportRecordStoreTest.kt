package com.acme.transport.emissions.carbonreport.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CarbonReportRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCarbonReportRecordStore()
        val item = newCarbonReportRecord("code-193", "label-983")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
