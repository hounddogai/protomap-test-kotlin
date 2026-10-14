package com.acme.transport.crewing.roster.fallbackrosterforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryRosterProposalTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryRosterProposal("code-453", "name-18")
        assertEquals(PrimaryRosterProposalStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPrimaryRosterProposal("code-453", "name-18")
        try {
            item.moveTo(PrimaryRosterProposalStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryRosterProposalStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryRosterProposal("code-453", "name-18")
        item.status = PrimaryRosterProposalStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryRosterProposalStatus.REQUESTED) }
    }
}
