package com.acme.transport.customs.inspection.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedInspectionRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedInspectionRollup("code-288", "title-211")
        val view = toWeightedInspectionRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedInspectionRollupViews(mutableListOf<WeightedInspectionRollup>(item, item)).size, "views")
    }
}
