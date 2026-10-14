package com.acme.transport.freightpay.dispute.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedDisputeEstimateTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedDisputeEstimate("code-92", "name-177")
        assertEquals(BatchedDisputeEstimateStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newBatchedDisputeEstimate("code-92", "name-177")
        try {
            item.moveTo(BatchedDisputeEstimateStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedDisputeEstimateStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedDisputeEstimate("code-92", "name-177")
        item.status = BatchedDisputeEstimateStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedDisputeEstimateStatus.OPEN) }
    }
}
