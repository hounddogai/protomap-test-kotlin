package com.acme.transport.fleetops.defect.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictDefectBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictDefectBatchStore()
        val item = newStrictDefectBatch("code-781", "name-505")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
