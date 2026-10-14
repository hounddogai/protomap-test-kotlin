package com.acme.transport.customs.inspection.inspectionrule

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedInspectionAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedInspectionAllocation("code-936", "name-927")
        val view = toWeightedInspectionAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedInspectionAllocationViews(mutableListOf<WeightedInspectionAllocation>(item, item)).size, "views")
    }
}
