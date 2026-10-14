package com.acme.transport.safety.scorecard.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class ScorecardProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScorecardProposal("code-864", "label-895")
        val view = toScorecardProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScorecardProposalViews(mutableListOf<ScorecardProposal>(item, item)).size, "views")
    }
}
