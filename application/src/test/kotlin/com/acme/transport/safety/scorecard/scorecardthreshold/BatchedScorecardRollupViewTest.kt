package com.acme.transport.safety.scorecard.scorecardthreshold

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedScorecardRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedScorecardRollup("code-296", "label-963")
        val view = toBatchedScorecardRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedScorecardRollupViews(mutableListOf<BatchedScorecardRollup>(item, item)).size, "views")
    }
}
