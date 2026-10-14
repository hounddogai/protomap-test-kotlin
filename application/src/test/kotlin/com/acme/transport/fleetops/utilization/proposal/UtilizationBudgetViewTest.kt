package com.acme.transport.fleetops.utilization.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilizationBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newUtilizationBudget("code-116", "name-337")
        val view = toUtilizationBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toUtilizationBudgetViews(mutableListOf<UtilizationBudget>(item, item)).size, "views")
    }
}
