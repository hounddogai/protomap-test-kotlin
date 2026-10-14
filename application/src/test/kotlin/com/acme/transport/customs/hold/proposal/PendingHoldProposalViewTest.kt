package com.acme.transport.customs.hold.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingHoldProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingHoldProposal("code-160", "name-129")
        val view = toPendingHoldProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingHoldProposalViews(mutableListOf<PendingHoldProposal>(item, item)).size, "views")
    }
}
