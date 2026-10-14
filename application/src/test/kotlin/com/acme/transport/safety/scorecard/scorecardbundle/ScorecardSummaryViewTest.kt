package com.acme.transport.safety.scorecard.scorecardbundle

import kotlin.test.Test
import kotlin.test.assertEquals

class ScorecardSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScorecardSummary("code-580", "name-967")
        val view = toScorecardSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScorecardSummaryViews(mutableListOf<ScorecardSummary>(item, item)).size, "views")
    }
}
