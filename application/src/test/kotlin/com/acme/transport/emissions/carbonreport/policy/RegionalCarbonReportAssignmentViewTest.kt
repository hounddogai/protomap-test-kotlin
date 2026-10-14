package com.acme.transport.emissions.carbonreport.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalCarbonReportAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalCarbonReportAssignment("code-265", "name-613")
        val view = toRegionalCarbonReportAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalCarbonReportAssignmentViews(mutableListOf<RegionalCarbonReportAssignment>(item, item)).size, "views")
    }
}
