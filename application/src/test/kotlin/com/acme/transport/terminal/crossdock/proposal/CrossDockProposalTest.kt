package com.acme.transport.terminal.crossdock.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockProposalTest {
    @Test
    fun `starts in new`() {
        val item = newCrossDockProposal("code-535", "name-775")
        assertEquals(CrossDockProposalStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newCrossDockProposal("code-535", "name-775")
        try {
            item.moveTo(CrossDockProposalStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockProposalStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCrossDockProposal("code-535", "name-775")
        item.status = CrossDockProposalStatus.FAILED
        assertFails("move") { item.moveTo(CrossDockProposalStatus.NEW) }
    }
}
