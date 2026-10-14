package com.acme.transport.crewing.dutyperiod.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingDutyPeriodBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingDutyPeriodBudget("code-645", "title-347")
        val view = toRollingDutyPeriodBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingDutyPeriodBudgetViews(mutableListOf<RollingDutyPeriodBudget>(item, item)).size, "views")
    }
}
