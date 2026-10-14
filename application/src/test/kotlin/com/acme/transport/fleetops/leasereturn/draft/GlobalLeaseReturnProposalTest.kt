package com.acme.transport.fleetops.leasereturn.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalLeaseReturnProposalTest {
    @Test
    fun `starts in open`() {
        val item = newGlobalLeaseReturnProposal("code-716", "label-701")
        assertEquals(GlobalLeaseReturnProposalStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newGlobalLeaseReturnProposal("code-716", "label-701")
        try {
            item.moveTo(GlobalLeaseReturnProposalStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalLeaseReturnProposalStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newGlobalLeaseReturnProposal("code-716", "label-701")
        item.status = GlobalLeaseReturnProposalStatus.ARCHIVED
        assertFails("move") { item.moveTo(GlobalLeaseReturnProposalStatus.OPEN) }
    }
}
