package com.acme.transport.emissions.carbonreport.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultCarbonReportMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultCarbonReportMilestoneStore()
        val item = newDefaultCarbonReportMilestone("code-819", "title-808")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
