package com.acme.transport.fleetops.inspection.inspectionschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalInspectionBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalInspectionBatchStore()
        val item = newGlobalInspectionBatch("code-888", "label-62")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
