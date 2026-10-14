package com.acme.transport.customs.inspection.inspectiondigest

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveInspectionAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveInspectionAllocation("code-144", "name-225")
        val view = toAdaptiveInspectionAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveInspectionAllocationViews(mutableListOf<AdaptiveInspectionAllocation>(item, item)).size, "views")
    }
}
