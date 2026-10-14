package com.acme.transport.freightpay.dispute.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryDisputeMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryDisputeMetricStore()
        val item = newPrimaryDisputeMetric("code-624", "label-58")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
