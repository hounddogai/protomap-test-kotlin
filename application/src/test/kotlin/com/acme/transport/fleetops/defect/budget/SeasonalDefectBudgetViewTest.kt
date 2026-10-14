package com.acme.transport.fleetops.defect.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalDefectBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalDefectBudget("code-380", "title-806")
        val view = toSeasonalDefectBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalDefectBudgetViews(mutableListOf<SeasonalDefectBudget>(item, item)).size, "views")
    }
}
