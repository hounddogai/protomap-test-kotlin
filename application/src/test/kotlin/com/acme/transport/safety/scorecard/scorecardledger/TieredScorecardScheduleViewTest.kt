package com.acme.transport.safety.scorecard.scorecardledger

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredScorecardScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredScorecardSchedule("code-628", "title-493")
        val view = toTieredScorecardScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredScorecardScheduleViews(mutableListOf<TieredScorecardSchedule>(item, item)).size, "views")
    }
}
