package com.acme.transport.linehaul.laneplan.planwindow

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalLanePlanBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalLanePlanBudget("code-84", "label-774")
        val view = toRegionalLanePlanBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalLanePlanBudgetViews(mutableListOf<RegionalLanePlanBudget>(item, item)).size, "views")
    }
}
