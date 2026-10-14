package com.acme.transport.fleetops.inspection.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionEstimate("code-800", "label-137")
        val view = toInspectionEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionEstimateViews(mutableListOf<InspectionEstimate>(item, item)).size, "views")
    }
}
