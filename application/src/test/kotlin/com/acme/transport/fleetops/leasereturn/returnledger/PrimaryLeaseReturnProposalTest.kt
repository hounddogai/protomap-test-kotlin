package com.acme.transport.fleetops.leasereturn.returnledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryLeaseReturnProposalTest {
    @Test
    fun `starts in planned`() {
        val item = newPrimaryLeaseReturnProposal("code-828", "title-698")
        assertEquals(PrimaryLeaseReturnProposalStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newPrimaryLeaseReturnProposal("code-828", "title-698")
        try {
            item.moveTo(PrimaryLeaseReturnProposalStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryLeaseReturnProposalStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPrimaryLeaseReturnProposal("code-828", "title-698")
        item.status = PrimaryLeaseReturnProposalStatus.CANCELLED
        assertFails("move") { item.moveTo(PrimaryLeaseReturnProposalStatus.PLANNED) }
    }
}
