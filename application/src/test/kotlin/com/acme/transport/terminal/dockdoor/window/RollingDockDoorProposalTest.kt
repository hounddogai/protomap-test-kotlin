package com.acme.transport.terminal.dockdoor.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingDockDoorProposalTest {
    @Test
    fun `starts in planned`() {
        val item = newRollingDockDoorProposal("code-237", "name-209")
        assertEquals(RollingDockDoorProposalStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newRollingDockDoorProposal("code-237", "name-209")
        try {
            item.moveTo(RollingDockDoorProposalStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingDockDoorProposalStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRollingDockDoorProposal("code-237", "name-209")
        item.status = RollingDockDoorProposalStatus.CANCELLED
        assertFails("move") { item.moveTo(RollingDockDoorProposalStatus.PLANNED) }
    }
}
