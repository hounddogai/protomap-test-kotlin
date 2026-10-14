package com.acme.transport.crewing.dutyperiod.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalDutyPeriodEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalDutyPeriodEstimate("code-454", "title-455")
        val view = toGlobalDutyPeriodEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalDutyPeriodEstimateViews(mutableListOf<GlobalDutyPeriodEstimate>(item, item)).size, "views")
    }
}
