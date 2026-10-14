package com.acme.transport.crewing.dutyperiod.periodnotice

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveDutyPeriodSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveDutyPeriodSegment("code-24", "name-362")
        val view = toAdaptiveDutyPeriodSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveDutyPeriodSegmentViews(mutableListOf<AdaptiveDutyPeriodSegment>(item, item)).size, "views")
    }
}
