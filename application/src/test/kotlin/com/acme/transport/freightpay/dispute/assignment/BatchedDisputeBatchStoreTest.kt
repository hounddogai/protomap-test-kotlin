package com.acme.transport.freightpay.dispute.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedDisputeBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedDisputeBatchStore()
        val item = newBatchedDisputeBatch("code-409", "label-437")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
