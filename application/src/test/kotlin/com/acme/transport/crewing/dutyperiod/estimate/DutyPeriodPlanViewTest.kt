package com.acme.transport.crewing.dutyperiod.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class DutyPeriodPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDutyPeriodPlan("code-485", "label-745")
        val view = toDutyPeriodPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDutyPeriodPlanViews(mutableListOf<DutyPeriodPlan>(item, item)).size, "views")
    }
}
