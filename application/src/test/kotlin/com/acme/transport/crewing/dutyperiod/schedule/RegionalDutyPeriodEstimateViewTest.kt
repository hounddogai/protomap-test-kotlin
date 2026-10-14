package com.acme.transport.crewing.dutyperiod.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalDutyPeriodEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalDutyPeriodEstimate("code-389", "title-810")
        val view = toRegionalDutyPeriodEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalDutyPeriodEstimateViews(mutableListOf<RegionalDutyPeriodEstimate>(item, item)).size, "views")
    }
}
