package com.acme.transport.safety.scorecard.scorecardslot

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedScorecardBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedScorecardBundle("code-67", "title-829")
        val view = toBatchedScorecardBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedScorecardBundleViews(mutableListOf<BatchedScorecardBundle>(item, item)).size, "views")
    }
}
