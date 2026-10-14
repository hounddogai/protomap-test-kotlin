package com.acme.transport.crewing.dutyperiod.window

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalDutyPeriodBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalDutyPeriodBatch("code-299", "label-552")
        val view = toSeasonalDutyPeriodBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalDutyPeriodBatchViews(mutableListOf<SeasonalDutyPeriodBatch>(item, item)).size, "views")
    }
}
