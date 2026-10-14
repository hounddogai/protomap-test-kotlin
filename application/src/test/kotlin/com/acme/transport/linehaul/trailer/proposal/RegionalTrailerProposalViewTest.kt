package com.acme.transport.linehaul.trailer.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalTrailerProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalTrailerProposal("code-542", "name-934")
        val view = toRegionalTrailerProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalTrailerProposalViews(mutableListOf<RegionalTrailerProposal>(item, item)).size, "views")
    }
}
