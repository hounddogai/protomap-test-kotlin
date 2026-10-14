package com.acme.transport.fleetops.utilization.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedUtilizationMetricTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedUtilizationMetric("code-995", "title-414")
        assertEquals(BatchedUtilizationMetricStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newBatchedUtilizationMetric("code-995", "title-414")
        try {
            item.moveTo(BatchedUtilizationMetricStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedUtilizationMetricStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedUtilizationMetric("code-995", "title-414")
        item.status = BatchedUtilizationMetricStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedUtilizationMetricStatus.OPEN) }
    }
}
