package com.acme.transport.freightpay.dispute.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientDisputeMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientDisputeMetricStore()
        val item = newLenientDisputeMetric("code-154", "title-3")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
