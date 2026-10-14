package com.acme.transport.fleetops.inspection.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionSegmentStore()
        val item = newInspectionSegment("code-358", "label-852")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
