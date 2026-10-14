package com.acme.transport.emissions.carbonreport.reportscore

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedCarbonReportAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedCarbonReportAssignment("code-206", "label-943")
        val view = toCappedCarbonReportAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedCarbonReportAssignmentViews(mutableListOf<CappedCarbonReportAssignment>(item, item)).size, "views")
    }
}
