package com.acme.transport.fleetops.defect.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class DefectScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefectScore("code-172", "label-430")
        val view = toDefectScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefectScoreViews(mutableListOf<DefectScore>(item, item)).size, "views")
    }
}
