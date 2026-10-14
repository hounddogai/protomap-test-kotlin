package com.acme.transport.terminal.crossdock.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class CrossDockProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCrossDockProposal("code-535", "name-775")
        val view = toCrossDockProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCrossDockProposalViews(mutableListOf<CrossDockProposal>(item, item)).size, "views")
    }
}
