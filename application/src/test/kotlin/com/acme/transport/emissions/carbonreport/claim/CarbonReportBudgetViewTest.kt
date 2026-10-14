package com.acme.transport.emissions.carbonreport.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class CarbonReportBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCarbonReportBudget("code-64", "name-849")
        val view = toCarbonReportBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCarbonReportBudgetViews(mutableListOf<CarbonReportBudget>(item, item)).size, "views")
    }
}
