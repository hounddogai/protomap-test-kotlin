package com.acme.transport.emissions.carbonreport.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class CarbonReportRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCarbonReportRule("code-260", "name-933")
        val view = toCarbonReportRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCarbonReportRuleViews(mutableListOf<CarbonReportRule>(item, item)).size, "views")
    }
}
