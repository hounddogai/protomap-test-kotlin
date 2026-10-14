package com.acme.transport.fleetops.inspection.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedInspectionBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedInspectionBaseline("code-144", "label-855")
        val view = toWeightedInspectionBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedInspectionBaselineViews(mutableListOf<WeightedInspectionBaseline>(item, item)).size, "views")
    }
}
