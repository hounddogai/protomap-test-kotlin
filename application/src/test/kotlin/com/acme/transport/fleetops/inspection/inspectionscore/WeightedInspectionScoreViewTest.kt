package com.acme.transport.fleetops.inspection.inspectionscore

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedInspectionScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedInspectionScore("code-256", "label-46")
        val view = toWeightedInspectionScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedInspectionScoreViews(mutableListOf<WeightedInspectionScore>(item, item)).size, "views")
    }
}
