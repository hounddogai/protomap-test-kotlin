package com.acme.transport.crewing.dutyperiod.perioddraft

import kotlin.test.Test
import kotlin.test.assertEquals

class DutyPeriodAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDutyPeriodAdjustment("code-483", "label-184")
        val view = toDutyPeriodAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDutyPeriodAdjustmentViews(mutableListOf<DutyPeriodAdjustment>(item, item)).size, "views")
    }
}
