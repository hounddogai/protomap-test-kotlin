package com.acme.transport.fleetops.defect.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class DefectCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefectCycle("code-583", "label-222")
        val view = toDefectCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefectCycleViews(mutableListOf<DefectCycle>(item, item)).size, "views")
    }
}
