package com.acme.transport.customs.inspection.inspectionestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictInspectionBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictInspectionBatchStore()
        val item = newStrictInspectionBatch("code-692", "label-693")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
