package com.acme.transport.fleetops.defect.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefectBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefectBatchStore()
        val item = newDefectBatch("code-762", "name-150")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
