package com.acme.transport.fleetops.inspection.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientInspectionSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientInspectionSegmentStore()
        val item = newLenientInspectionSegment("code-499", "label-145")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
