package com.acme.transport.network.node.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedNodeAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedNodeAssignmentStore()
        val item = newBatchedNodeAssignment("code-833", "title-20")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
