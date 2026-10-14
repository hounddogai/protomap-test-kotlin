package com.acme.transport.fleetops.leasereturn.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnProposalTest {
    @Test
    fun `starts in proposed`() {
        val item = newLeaseReturnProposal("code-319", "label-824")
        assertEquals(LeaseReturnProposalStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newLeaseReturnProposal("code-319", "label-824")
        try {
            item.moveTo(LeaseReturnProposalStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnProposalStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLeaseReturnProposal("code-319", "label-824")
        item.status = LeaseReturnProposalStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LeaseReturnProposalStatus.PROPOSED) }
    }
}
