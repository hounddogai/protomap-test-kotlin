package com.acme.transport.safety.incident.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientIncidentProposalTest {
    @Test
    fun `starts in draft`() {
        val item = newLenientIncidentProposal("code-969", "name-749")
        assertEquals(LenientIncidentProposalStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newLenientIncidentProposal("code-969", "name-749")
        try {
            item.moveTo(LenientIncidentProposalStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientIncidentProposalStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLenientIncidentProposal("code-969", "name-749")
        item.status = LenientIncidentProposalStatus.CLOSED
        assertFails("move") { item.moveTo(LenientIncidentProposalStatus.DRAFT) }
    }
}
