package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedDisputeMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedDisputeMetricStore()
        val item = newBatchedDisputeMetric("code-334", "name-455")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
