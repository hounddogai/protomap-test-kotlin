package com.acme.transport.safety.scorecard.scorecardnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedScorecardProposalTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedScorecardProposal("code-604", "label-913")
        assertEquals(BatchedScorecardProposalStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newBatchedScorecardProposal("code-604", "label-913")
        try {
            item.moveTo(BatchedScorecardProposalStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedScorecardProposalStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedScorecardProposal("code-604", "label-913")
        item.status = BatchedScorecardProposalStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedScorecardProposalStatus.IDLE) }
    }
}
