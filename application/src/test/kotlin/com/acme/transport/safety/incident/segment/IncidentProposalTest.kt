package com.acme.transport.safety.incident.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentProposalTest {
    @Test
    fun `starts in created`() {
        val item = newIncidentProposal("code-553", "label-638")
        assertEquals(IncidentProposalStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newIncidentProposal("code-553", "label-638")
        try {
            item.moveTo(IncidentProposalStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentProposalStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newIncidentProposal("code-553", "label-638")
        item.status = IncidentProposalStatus.RETIRED
        assertFails("move") { item.moveTo(IncidentProposalStatus.CREATED) }
    }
}
