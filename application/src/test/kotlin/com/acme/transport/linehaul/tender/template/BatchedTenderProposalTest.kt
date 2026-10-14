package com.acme.transport.linehaul.tender.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedTenderProposalTest {
    @Test
    fun `starts in planned`() {
        val item = newBatchedTenderProposal("code-968", "title-739")
        assertEquals(BatchedTenderProposalStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newBatchedTenderProposal("code-968", "title-739")
        try {
            item.moveTo(BatchedTenderProposalStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedTenderProposalStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBatchedTenderProposal("code-968", "title-739")
        item.status = BatchedTenderProposalStatus.CANCELLED
        assertFails("move") { item.moveTo(BatchedTenderProposalStatus.PLANNED) }
    }
}
