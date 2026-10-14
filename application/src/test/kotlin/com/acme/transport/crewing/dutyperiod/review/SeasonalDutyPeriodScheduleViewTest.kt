package com.acme.transport.crewing.dutyperiod.review

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalDutyPeriodScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalDutyPeriodSchedule("code-997", "name-369")
        val view = toSeasonalDutyPeriodScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalDutyPeriodScheduleViews(mutableListOf<SeasonalDutyPeriodSchedule>(item, item)).size, "views")
    }
}
