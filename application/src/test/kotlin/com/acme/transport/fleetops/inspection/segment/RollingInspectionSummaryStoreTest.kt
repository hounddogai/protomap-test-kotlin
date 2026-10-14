package com.acme.transport.fleetops.inspection.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingInspectionSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingInspectionSummaryStore()
        val item = newRollingInspectionSummary("code-227", "title-948")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
