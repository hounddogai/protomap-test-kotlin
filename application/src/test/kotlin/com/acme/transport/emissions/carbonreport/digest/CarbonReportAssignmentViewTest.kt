package com.acme.transport.emissions.carbonreport.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class CarbonReportAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCarbonReportAssignment("code-220", "name-189")
        val view = toCarbonReportAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCarbonReportAssignmentViews(mutableListOf<CarbonReportAssignment>(item, item)).size, "views")
    }
}
