package com.acme.transport.fleetops.inspection.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackInspectionAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackInspectionAllocation("code-770", "name-7")
        val view = toFallbackInspectionAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackInspectionAllocationViews(mutableListOf<FallbackInspectionAllocation>(item, item)).size, "views")
    }
}
