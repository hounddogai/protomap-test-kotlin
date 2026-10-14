package com.acme.transport.crewing.dutyperiod.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveDutyPeriodMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveDutyPeriodMilestone("code-254", "title-233")
        val view = toAdaptiveDutyPeriodMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveDutyPeriodMilestoneViews(mutableListOf<AdaptiveDutyPeriodMilestone>(item, item)).size, "views")
    }
}
