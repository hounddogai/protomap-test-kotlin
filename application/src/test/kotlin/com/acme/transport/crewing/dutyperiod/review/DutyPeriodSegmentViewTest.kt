package com.acme.transport.crewing.dutyperiod.review

import kotlin.test.Test
import kotlin.test.assertEquals

class DutyPeriodSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDutyPeriodSegment("code-956", "title-626")
        val view = toDutyPeriodSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDutyPeriodSegmentViews(mutableListOf<DutyPeriodSegment>(item, item)).size, "views")
    }
}
