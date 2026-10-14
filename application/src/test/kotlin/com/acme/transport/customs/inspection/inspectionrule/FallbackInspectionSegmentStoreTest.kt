package com.acme.transport.customs.inspection.inspectionrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackInspectionSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackInspectionSegmentStore()
        val item = newFallbackInspectionSegment("code-249", "title-964")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
