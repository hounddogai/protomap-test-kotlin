package com.acme.transport.fleetops.inspection.score

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionScore("code-711", "label-392")
        val view = toInspectionScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionScoreViews(mutableListOf<InspectionScore>(item, item)).size, "views")
    }
}
