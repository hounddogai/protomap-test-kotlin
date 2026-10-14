package com.acme.transport.emissions.carbonreport.reportscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryCarbonReportCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryCarbonReportCycleStore()
        val item = newPrimaryCarbonReportCycle("code-109", "name-87")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
