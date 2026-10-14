package com.acme.transport.crewing.dutyperiod.periodsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class DutyPeriodMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDutyPeriodMilestone("code-460", "title-860")
        val view = toDutyPeriodMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDutyPeriodMilestoneViews(mutableListOf<DutyPeriodMilestone>(item, item)).size, "views")
    }
}
