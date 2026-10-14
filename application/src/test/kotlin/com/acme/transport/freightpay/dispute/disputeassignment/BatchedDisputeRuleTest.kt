package com.acme.transport.freightpay.dispute.disputeassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedDisputeRuleTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedDisputeRule("code-853", "name-239")
        assertEquals(BatchedDisputeRuleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newBatchedDisputeRule("code-853", "name-239")
        try {
            item.moveTo(BatchedDisputeRuleStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedDisputeRuleStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedDisputeRule("code-853", "name-239")
        item.status = BatchedDisputeRuleStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedDisputeRuleStatus.OPEN) }
    }
}
