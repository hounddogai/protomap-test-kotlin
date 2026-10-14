package com.acme.transport.linehaul.capacity.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedCapacityGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedCapacityGroupStore()
        val item = newBatchedCapacityGroup("code-554", "label-655")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
