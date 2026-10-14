package com.acme.transport.fleetops.inspection.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionSummaryStore()
        val item = newInspectionSummary("code-96", "title-61")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
