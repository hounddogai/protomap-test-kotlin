package com.acme.transport.fleetops.inspection.inspectionsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedInspectionSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedInspectionSummaryStore()
        val item = newCappedInspectionSummary("code-978", "title-696")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
