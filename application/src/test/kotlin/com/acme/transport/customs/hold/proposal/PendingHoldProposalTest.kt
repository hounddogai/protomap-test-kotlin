package com.acme.transport.customs.hold.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingHoldProposalTest {
    @Test
    fun `starts in draft`() {
        val item = newPendingHoldProposal("code-160", "name-129")
        assertEquals(PendingHoldProposalStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newPendingHoldProposal("code-160", "name-129")
        try {
            item.moveTo(PendingHoldProposalStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingHoldProposalStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPendingHoldProposal("code-160", "name-129")
        item.status = PendingHoldProposalStatus.CLOSED
        assertFails("move") { item.moveTo(PendingHoldProposalStatus.DRAFT) }
    }
}
