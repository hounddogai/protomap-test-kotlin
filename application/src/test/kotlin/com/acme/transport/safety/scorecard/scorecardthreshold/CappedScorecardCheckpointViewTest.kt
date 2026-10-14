package com.acme.transport.safety.scorecard.scorecardthreshold

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedScorecardCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedScorecardCheckpoint("code-134", "label-739")
        val view = toCappedScorecardCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedScorecardCheckpointViews(mutableListOf<CappedScorecardCheckpoint>(item, item)).size, "views")
    }
}
