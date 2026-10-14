package com.acme.transport.crewing.roster.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterProposalTest {
    @Test
    fun `starts in created`() {
        val item = newRosterProposal("code-657", "title-535")
        assertEquals(RosterProposalStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRosterProposal("code-657", "title-535")
        try {
            item.moveTo(RosterProposalStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterProposalStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRosterProposal("code-657", "title-535")
        item.status = RosterProposalStatus.RETIRED
        assertFails("move") { item.moveTo(RosterProposalStatus.CREATED) }
    }
}
