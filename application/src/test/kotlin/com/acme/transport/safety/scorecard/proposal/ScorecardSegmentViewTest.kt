package com.acme.transport.safety.scorecard.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class ScorecardSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScorecardSegment("code-744", "label-179")
        val view = toScorecardSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScorecardSegmentViews(mutableListOf<ScorecardSegment>(item, item)).size, "views")
    }
}
