package com.acme.transport.crewing.roster.fallbackrosterforecast

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryRosterProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryRosterProposal("code-453", "name-18")
        val view = toPrimaryRosterProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryRosterProposalViews(mutableListOf<PrimaryRosterProposal>(item, item)).size, "views")
    }
}
