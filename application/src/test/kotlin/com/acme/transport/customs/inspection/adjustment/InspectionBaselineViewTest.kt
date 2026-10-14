package com.acme.transport.customs.inspection.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionBaseline("code-720", "label-477")
        val view = toInspectionBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionBaselineViews(mutableListOf<InspectionBaseline>(item, item)).size, "views")
    }
}
