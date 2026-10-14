package com.acme.transport.lastmile.proofofdelivery.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedProofOfDeliveryBudgetTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedProofOfDeliveryBudget("code-229", "label-866")
        assertEquals(BatchedProofOfDeliveryBudgetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newBatchedProofOfDeliveryBudget("code-229", "label-866")
        try {
            item.moveTo(BatchedProofOfDeliveryBudgetStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedProofOfDeliveryBudgetStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedProofOfDeliveryBudget("code-229", "label-866")
        item.status = BatchedProofOfDeliveryBudgetStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedProofOfDeliveryBudgetStatus.OPEN) }
    }
}
