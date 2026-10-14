package com.acme.transport.freightpay.dispute.disputeassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedDisputeRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedDisputeRuleStore()
        val item = newBatchedDisputeRule("code-853", "name-239")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
