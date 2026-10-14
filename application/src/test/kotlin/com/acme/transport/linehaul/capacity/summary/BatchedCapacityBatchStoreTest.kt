package com.acme.transport.linehaul.capacity.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedCapacityBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedCapacityBatchStore()
        val item = newBatchedCapacityBatch("code-170", "title-598")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
