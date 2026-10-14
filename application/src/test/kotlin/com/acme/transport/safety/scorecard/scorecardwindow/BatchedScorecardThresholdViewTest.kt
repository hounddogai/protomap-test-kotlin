package com.acme.transport.safety.scorecard.scorecardwindow

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedScorecardThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedScorecardThreshold("code-987", "name-96")
        val view = toBatchedScorecardThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedScorecardThresholdViews(mutableListOf<BatchedScorecardThreshold>(item, item)).size, "views")
    }
}
