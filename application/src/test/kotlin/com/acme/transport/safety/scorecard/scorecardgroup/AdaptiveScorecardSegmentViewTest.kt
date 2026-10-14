package com.acme.transport.safety.scorecard.scorecardgroup

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveScorecardSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveScorecardSegment("code-904", "title-718")
        val view = toAdaptiveScorecardSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveScorecardSegmentViews(mutableListOf<AdaptiveScorecardSegment>(item, item)).size, "views")
    }
}
