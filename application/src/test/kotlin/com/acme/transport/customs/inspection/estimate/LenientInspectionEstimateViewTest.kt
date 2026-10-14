package com.acme.transport.customs.inspection.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientInspectionEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientInspectionEstimate("code-945", "title-390")
        val view = toLenientInspectionEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientInspectionEstimateViews(mutableListOf<LenientInspectionEstimate>(item, item)).size, "views")
    }
}
