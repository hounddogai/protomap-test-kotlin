package com.acme.transport.emissions.carbonreport.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalCarbonReportRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalCarbonReportRequestStore()
        val item = newRegionalCarbonReportRequest("code-926", "title-468")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
