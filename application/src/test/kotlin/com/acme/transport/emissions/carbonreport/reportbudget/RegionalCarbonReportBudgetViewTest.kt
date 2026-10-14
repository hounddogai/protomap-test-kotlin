package com.acme.transport.emissions.carbonreport.reportbudget

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalCarbonReportBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalCarbonReportBudget("code-161", "label-817")
        val view = toRegionalCarbonReportBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalCarbonReportBudgetViews(mutableListOf<RegionalCarbonReportBudget>(item, item)).size, "views")
    }
}
