package com.acme.transport.terminal.dockdoor.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDockDoorProposalTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalDockDoorProposal("code-430", "name-8")
        assertEquals(RegionalDockDoorProposalStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newRegionalDockDoorProposal("code-430", "name-8")
        try {
            item.moveTo(RegionalDockDoorProposalStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDockDoorProposalStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalDockDoorProposal("code-430", "name-8")
        item.status = RegionalDockDoorProposalStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalDockDoorProposalStatus.REQUESTED) }
    }
}
