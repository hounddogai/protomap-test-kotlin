package com.acme.transport.lastmile.locker.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class LockerBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLockerBudget("code-329", "name-92")
        val view = toLockerBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLockerBudgetViews(mutableListOf<LockerBudget>(item, item)).size, "views")
    }
}
