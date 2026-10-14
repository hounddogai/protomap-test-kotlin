package com.acme.transport.linehaul.capacity.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedCapacitySegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedCapacitySegmentStore()
        val item = newBatchedCapacitySegment("code-119", "name-656")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
