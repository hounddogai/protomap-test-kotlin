package com.acme.transport.crewing.dutyperiod.periodassignment

import kotlin.test.Test
import kotlin.test.assertEquals

class DutyPeriodCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDutyPeriodCycle("code-956", "title-9")
        val view = toDutyPeriodCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDutyPeriodCycleViews(mutableListOf<DutyPeriodCycle>(item, item)).size, "views")
    }
}
