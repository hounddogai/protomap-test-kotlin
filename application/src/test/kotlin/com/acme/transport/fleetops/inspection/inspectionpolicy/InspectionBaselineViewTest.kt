package com.acme.transport.fleetops.inspection.inspectionpolicy

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionBaseline("code-111", "label-69")
        val view = toInspectionBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionBaselineViews(mutableListOf<InspectionBaseline>(item, item)).size, "views")
    }
}
