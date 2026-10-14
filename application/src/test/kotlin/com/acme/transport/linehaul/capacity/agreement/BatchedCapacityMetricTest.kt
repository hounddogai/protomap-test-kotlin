package com.acme.transport.linehaul.capacity.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedCapacityMetricTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedCapacityMetric("code-61", "title-410")
        assertEquals(BatchedCapacityMetricStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newBatchedCapacityMetric("code-61", "title-410")
        try {
            item.moveTo(BatchedCapacityMetricStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedCapacityMetricStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedCapacityMetric("code-61", "title-410")
        item.status = BatchedCapacityMetricStatus.FAILED
        assertFails("move") { item.moveTo(BatchedCapacityMetricStatus.NEW) }
    }
}
