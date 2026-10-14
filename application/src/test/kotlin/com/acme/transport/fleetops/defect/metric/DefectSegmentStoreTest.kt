package com.acme.transport.fleetops.defect.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefectSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefectSegmentStore()
        val item = newDefectSegment("code-243", "label-348")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
