package com.acme.transport.emissions.carbonreport.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedCarbonReportCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedCarbonReportCycleStore()
        val item = newCappedCarbonReportCycle("code-721", "name-101")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
