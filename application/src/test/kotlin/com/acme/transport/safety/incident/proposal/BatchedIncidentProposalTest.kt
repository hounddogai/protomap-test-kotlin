package com.acme.transport.safety.incident.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedIncidentProposalTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedIncidentProposal("code-60", "label-458")
        assertEquals(BatchedIncidentProposalStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newBatchedIncidentProposal("code-60", "label-458")
        try {
            item.moveTo(BatchedIncidentProposalStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedIncidentProposalStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedIncidentProposal("code-60", "label-458")
        item.status = BatchedIncidentProposalStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedIncidentProposalStatus.OPEN) }
    }
}
