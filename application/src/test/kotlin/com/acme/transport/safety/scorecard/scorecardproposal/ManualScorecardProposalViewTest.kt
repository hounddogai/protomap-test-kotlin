package com.acme.transport.safety.scorecard.scorecardproposal

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualScorecardProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualScorecardProposal("code-584", "label-795")
        val view = toManualScorecardProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualScorecardProposalViews(mutableListOf<ManualScorecardProposal>(item, item)).size, "views")
    }
}
