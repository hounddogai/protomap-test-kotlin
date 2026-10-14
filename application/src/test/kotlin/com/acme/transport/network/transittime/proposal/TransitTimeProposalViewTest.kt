package com.acme.transport.network.transittime.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class TransitTimeProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTransitTimeProposal("code-136", "name-919")
        val view = toTransitTimeProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTransitTimeProposalViews(mutableListOf<TransitTimeProposal>(item, item)).size, "views")
    }
}
