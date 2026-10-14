package com.acme.transport.fleetops.defect.defecttarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedDefectProposalTest {
    @Test
    fun `starts in requested`() {
        val item = newBatchedDefectProposal("code-815", "name-406")
        assertEquals(BatchedDefectProposalStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newBatchedDefectProposal("code-815", "name-406")
        try {
            item.moveTo(BatchedDefectProposalStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedDefectProposalStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newBatchedDefectProposal("code-815", "name-406")
        item.status = BatchedDefectProposalStatus.EXPIRED
        assertFails("move") { item.moveTo(BatchedDefectProposalStatus.REQUESTED) }
    }
}
