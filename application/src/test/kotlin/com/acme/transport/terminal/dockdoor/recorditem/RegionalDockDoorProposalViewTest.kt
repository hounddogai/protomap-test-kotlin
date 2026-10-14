package com.acme.transport.terminal.dockdoor.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalDockDoorProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalDockDoorProposal("code-430", "name-8")
        val view = toRegionalDockDoorProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalDockDoorProposalViews(mutableListOf<RegionalDockDoorProposal>(item, item)).size, "views")
    }
}
