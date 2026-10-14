package com.acme.transport.emissions.carbonreport.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CarbonReportCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCarbonReportCycleStore()
        val item = newCarbonReportCycle("code-608", "label-985")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
