package com.acme.transport.fleetops.leasereturn.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class LeaseReturnProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLeaseReturnProposal("code-319", "label-824")
        val view = toLeaseReturnProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLeaseReturnProposalViews(mutableListOf<LeaseReturnProposal>(item, item)).size, "views")
    }
}
