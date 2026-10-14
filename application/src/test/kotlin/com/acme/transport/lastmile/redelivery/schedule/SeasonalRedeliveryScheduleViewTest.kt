package com.acme.transport.lastmile.redelivery.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalRedeliveryScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalRedeliverySchedule("code-537", "name-869")
        val view = toSeasonalRedeliveryScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalRedeliveryScheduleViews(mutableListOf<SeasonalRedeliverySchedule>(item, item)).size, "views")
    }
}
