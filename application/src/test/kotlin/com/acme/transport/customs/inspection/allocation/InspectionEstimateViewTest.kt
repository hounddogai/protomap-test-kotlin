package com.acme.transport.customs.inspection.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionEstimate("code-405", "title-178")
        val view = toInspectionEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionEstimateViews(mutableListOf<InspectionEstimate>(item, item)).size, "views")
    }
}
