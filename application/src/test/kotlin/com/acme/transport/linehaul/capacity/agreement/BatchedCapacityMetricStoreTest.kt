package com.acme.transport.linehaul.capacity.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedCapacityMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedCapacityMetricStore()
        val item = newBatchedCapacityMetric("code-61", "title-410")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
