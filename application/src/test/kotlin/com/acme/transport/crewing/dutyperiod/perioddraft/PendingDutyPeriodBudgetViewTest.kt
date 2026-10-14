package com.acme.transport.crewing.dutyperiod.perioddraft

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingDutyPeriodBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingDutyPeriodBudget("code-871", "name-163")
        val view = toPendingDutyPeriodBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingDutyPeriodBudgetViews(mutableListOf<PendingDutyPeriodBudget>(item, item)).size, "views")
    }
}
