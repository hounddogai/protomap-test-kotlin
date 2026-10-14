package com.acme.transport.lastmile.locker.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledLockerBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledLockerBudget("code-345", "name-52")
        val view = toScheduledLockerBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledLockerBudgetViews(mutableListOf<ScheduledLockerBudget>(item, item)).size, "views")
    }
}
