package com.acme.transport.linehaul.capacity.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientCapacityProposalTest {
    @Test
    fun `starts in draft`() {
        val item = newLenientCapacityProposal("code-451", "title-431")
        assertEquals(LenientCapacityProposalStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newLenientCapacityProposal("code-451", "title-431")
        try {
            item.moveTo(LenientCapacityProposalStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientCapacityProposalStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLenientCapacityProposal("code-451", "title-431")
        item.status = LenientCapacityProposalStatus.CLOSED
        assertFails("move") { item.moveTo(LenientCapacityProposalStatus.DRAFT) }
    }
}
