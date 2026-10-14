package com.acme.transport.emissions.carbonreport.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingCarbonReportMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingCarbonReportMilestoneStore()
        val item = newPendingCarbonReportMilestone("code-215", "title-741")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
