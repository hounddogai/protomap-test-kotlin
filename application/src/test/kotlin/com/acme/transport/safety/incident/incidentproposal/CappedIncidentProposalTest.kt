package com.acme.transport.safety.incident.incidentproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedIncidentProposalTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedIncidentProposal("code-841", "label-906")
        assertEquals(CappedIncidentProposalStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newCappedIncidentProposal("code-841", "label-906")
        try {
            item.moveTo(CappedIncidentProposalStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedIncidentProposalStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedIncidentProposal("code-841", "label-906")
        item.status = CappedIncidentProposalStatus.CLOSED
        assertFails("move") { item.moveTo(CappedIncidentProposalStatus.DRAFT) }
    }
}
