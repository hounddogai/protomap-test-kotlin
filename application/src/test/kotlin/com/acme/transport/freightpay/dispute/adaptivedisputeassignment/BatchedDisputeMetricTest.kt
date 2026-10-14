package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedDisputeMetricTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedDisputeMetric("code-334", "name-455")
        assertEquals(BatchedDisputeMetricStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newBatchedDisputeMetric("code-334", "name-455")
        try {
            item.moveTo(BatchedDisputeMetricStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedDisputeMetricStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedDisputeMetric("code-334", "name-455")
        item.status = BatchedDisputeMetricStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedDisputeMetricStatus.OPEN) }
    }
}
