package com.acme.transport.crewing.dutyperiod.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledDutyPeriodEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledDutyPeriodEstimate("code-593", "title-768")
        val view = toScheduledDutyPeriodEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledDutyPeriodEstimateViews(mutableListOf<ScheduledDutyPeriodEstimate>(item, item)).size, "views")
    }
}
