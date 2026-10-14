package com.acme.transport.safety.scorecard.scorecardnotice

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedScorecardProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedScorecardProposal("code-604", "label-913")
        val view = toBatchedScorecardProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedScorecardProposalViews(mutableListOf<BatchedScorecardProposal>(item, item)).size, "views")
    }
}
