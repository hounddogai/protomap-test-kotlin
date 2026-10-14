package com.acme.transport.safety.incident.incidentsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveIncidentProposalTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveIncidentProposal("code-315", "label-211")
        assertEquals(AdaptiveIncidentProposalStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newAdaptiveIncidentProposal("code-315", "label-211")
        try {
            item.moveTo(AdaptiveIncidentProposalStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveIncidentProposalStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveIncidentProposal("code-315", "label-211")
        item.status = AdaptiveIncidentProposalStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveIncidentProposalStatus.DRAFT) }
    }
}
