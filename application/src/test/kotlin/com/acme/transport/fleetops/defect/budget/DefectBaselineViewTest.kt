package com.acme.transport.fleetops.defect.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class DefectBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefectBaseline("code-572", "label-334")
        val view = toDefectBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefectBaselineViews(mutableListOf<DefectBaseline>(item, item)).size, "views")
    }
}
