package com.acme.transport.fleetops.serviceplan.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalServicePlanBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalServicePlanBudget("code-983", "name-49")
        val view = toSeasonalServicePlanBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalServicePlanBudgetViews(mutableListOf<SeasonalServicePlanBudget>(item, item)).size, "views")
    }
}
