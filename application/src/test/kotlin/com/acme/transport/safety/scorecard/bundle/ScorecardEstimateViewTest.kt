package com.acme.transport.safety.scorecard.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class ScorecardEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScorecardEstimate("code-609", "label-691")
        val view = toScorecardEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScorecardEstimateViews(mutableListOf<ScorecardEstimate>(item, item)).size, "views")
    }
}
