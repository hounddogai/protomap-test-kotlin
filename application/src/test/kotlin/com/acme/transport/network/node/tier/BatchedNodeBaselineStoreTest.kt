package com.acme.transport.network.node.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedNodeBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedNodeBaselineStore()
        val item = newBatchedNodeBaseline("code-616", "title-935")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
