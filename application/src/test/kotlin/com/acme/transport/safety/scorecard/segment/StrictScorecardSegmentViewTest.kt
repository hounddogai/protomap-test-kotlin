package com.acme.transport.safety.scorecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictScorecardSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictScorecardSegment("code-222", "label-905")
        val view = toStrictScorecardSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictScorecardSegmentViews(mutableListOf<StrictScorecardSegment>(item, item)).size, "views")
    }
}
