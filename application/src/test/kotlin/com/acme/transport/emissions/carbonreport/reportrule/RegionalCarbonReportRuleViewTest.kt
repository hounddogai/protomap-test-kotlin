package com.acme.transport.emissions.carbonreport.reportrule

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalCarbonReportRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalCarbonReportRule("code-795", "label-262")
        val view = toRegionalCarbonReportRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalCarbonReportRuleViews(mutableListOf<RegionalCarbonReportRule>(item, item)).size, "views")
    }
}
