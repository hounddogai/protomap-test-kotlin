package com.acme.transport.lastmile.redelivery.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryProposalTest {
    @Test
    fun `starts in planned`() {
        val item = newRedeliveryProposal("code-880", "name-370")
        assertEquals(RedeliveryProposalStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newRedeliveryProposal("code-880", "name-370")
        try {
            item.moveTo(RedeliveryProposalStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryProposalStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRedeliveryProposal("code-880", "name-370")
        item.status = RedeliveryProposalStatus.CANCELLED
        assertFails("move") { item.moveTo(RedeliveryProposalStatus.PLANNED) }
    }
}
