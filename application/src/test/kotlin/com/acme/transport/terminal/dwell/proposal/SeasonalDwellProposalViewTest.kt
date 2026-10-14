package com.acme.transport.terminal.dwell.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalDwellProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalDwellProposal("code-54", "title-58")
        val view = toSeasonalDwellProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalDwellProposalViews(mutableListOf<SeasonalDwellProposal>(item, item)).size, "views")
    }
}
