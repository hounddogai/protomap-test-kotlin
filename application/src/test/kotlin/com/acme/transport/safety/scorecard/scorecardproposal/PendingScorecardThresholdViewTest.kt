package com.acme.transport.safety.scorecard.scorecardproposal

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingScorecardThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingScorecardThreshold("code-303", "title-406")
        val view = toPendingScorecardThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingScorecardThresholdViews(mutableListOf<PendingScorecardThreshold>(item, item)).size, "views")
    }
}
