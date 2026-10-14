package com.acme.transport.fleetops.inspection.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionBatchStore()
        val item = newInspectionBatch("code-336", "name-171")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
