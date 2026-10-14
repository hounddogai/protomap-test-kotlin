package com.acme.transport.emissions.carbonreport.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledCarbonReportCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledCarbonReportCycleStore()
        val item = newScheduledCarbonReportCycle("code-146", "title-183")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
