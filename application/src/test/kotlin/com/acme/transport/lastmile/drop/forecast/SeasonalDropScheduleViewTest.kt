package com.acme.transport.lastmile.drop.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalDropScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalDropSchedule("code-329", "label-729")
        val view = toSeasonalDropScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalDropScheduleViews(mutableListOf<SeasonalDropSchedule>(item, item)).size, "views")
    }
}
