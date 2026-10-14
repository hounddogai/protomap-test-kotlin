package com.acme.transport.fleetops.inspection.inspectiongroup

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultInspectionEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultInspectionEstimate("code-969", "name-678")
        val view = toDefaultInspectionEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultInspectionEstimateViews(mutableListOf<DefaultInspectionEstimate>(item, item)).size, "views")
    }
}
