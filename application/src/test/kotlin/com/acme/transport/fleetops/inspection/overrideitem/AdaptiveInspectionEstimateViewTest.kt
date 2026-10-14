package com.acme.transport.fleetops.inspection.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveInspectionEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveInspectionEstimate("code-739", "title-741")
        val view = toAdaptiveInspectionEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveInspectionEstimateViews(mutableListOf<AdaptiveInspectionEstimate>(item, item)).size, "views")
    }
}
