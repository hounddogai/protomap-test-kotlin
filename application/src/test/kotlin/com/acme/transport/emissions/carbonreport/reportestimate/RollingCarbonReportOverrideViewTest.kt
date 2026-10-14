package com.acme.transport.emissions.carbonreport.reportestimate

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingCarbonReportOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingCarbonReportOverride("code-765", "label-183")
        val view = toRollingCarbonReportOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingCarbonReportOverrideViews(mutableListOf<RollingCarbonReportOverride>(item, item)).size, "views")
    }
}
