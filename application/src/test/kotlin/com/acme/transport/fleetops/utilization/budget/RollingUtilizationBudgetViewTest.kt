package com.acme.transport.fleetops.utilization.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingUtilizationBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingUtilizationBudget("code-629", "title-723")
        val view = toRollingUtilizationBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingUtilizationBudgetViews(mutableListOf<RollingUtilizationBudget>(item, item)).size, "views")
    }
}
