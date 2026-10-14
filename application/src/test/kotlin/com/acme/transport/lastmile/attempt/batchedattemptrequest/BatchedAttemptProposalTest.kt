package com.acme.transport.lastmile.attempt.batchedattemptrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedAttemptProposalTest {
    @Test
    fun `starts in requested`() {
        val item = newBatchedAttemptProposal("code-976", "label-357")
        assertEquals(BatchedAttemptProposalStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newBatchedAttemptProposal("code-976", "label-357")
        try {
            item.moveTo(BatchedAttemptProposalStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedAttemptProposalStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newBatchedAttemptProposal("code-976", "label-357")
        item.status = BatchedAttemptProposalStatus.EXPIRED
        assertFails("move") { item.moveTo(BatchedAttemptProposalStatus.REQUESTED) }
    }
}
