package com.acme.transport.crewing.dutyperiod.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class DutyPeriodScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDutyPeriodSchedule("code-932", "label-594")
        val view = toDutyPeriodScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDutyPeriodScheduleViews(mutableListOf<DutyPeriodSchedule>(item, item)).size, "views")
    }
}
