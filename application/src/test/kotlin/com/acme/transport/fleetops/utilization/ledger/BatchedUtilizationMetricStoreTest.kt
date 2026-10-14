package com.acme.transport.fleetops.utilization.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedUtilizationMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedUtilizationMetricStore()
        val item = newBatchedUtilizationMetric("code-995", "title-414")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
