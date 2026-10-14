package com.acme.transport.emissions.carbonreport.strictreportadjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalCarbonReportPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalCarbonReportPolicy("code-660", "name-777")
        val view = toGlobalCarbonReportPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalCarbonReportPolicyViews(mutableListOf<GlobalCarbonReportPolicy>(item, item)).size, "views")
    }
}
