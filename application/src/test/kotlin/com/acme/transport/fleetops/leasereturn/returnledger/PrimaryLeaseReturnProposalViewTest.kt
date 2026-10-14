package com.acme.transport.fleetops.leasereturn.returnledger

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryLeaseReturnProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryLeaseReturnProposal("code-828", "title-698")
        val view = toPrimaryLeaseReturnProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryLeaseReturnProposalViews(mutableListOf<PrimaryLeaseReturnProposal>(item, item)).size, "views")
    }
}
