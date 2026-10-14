package com.acme.transport.customs.inspection.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionAllocation("code-725", "name-617")
        val view = toInspectionAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionAllocationViews(mutableListOf<InspectionAllocation>(item, item)).size, "views")
    }
}
