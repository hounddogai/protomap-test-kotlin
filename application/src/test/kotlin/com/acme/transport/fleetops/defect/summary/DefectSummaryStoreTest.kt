package com.acme.transport.fleetops.defect.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefectSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefectSummaryStore()
        val item = newDefectSummary("code-488", "name-181")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
