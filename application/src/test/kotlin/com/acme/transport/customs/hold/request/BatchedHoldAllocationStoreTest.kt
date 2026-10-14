package com.acme.transport.customs.hold.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedHoldAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedHoldAllocationStore()
        val item = newBatchedHoldAllocation("code-182", "title-361")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
