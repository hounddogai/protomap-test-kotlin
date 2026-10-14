package com.acme.transport.freightpay.accessorial.scheduledaccessorialmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedAccessorialMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedAccessorialMetricStore()
        val item = newBatchedAccessorialMetric("code-536", "label-744")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
