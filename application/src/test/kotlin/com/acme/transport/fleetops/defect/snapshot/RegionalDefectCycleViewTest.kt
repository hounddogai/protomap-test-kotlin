package com.acme.transport.fleetops.defect.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalDefectCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalDefectCycle("code-832", "label-773")
        val view = toRegionalDefectCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalDefectCycleViews(mutableListOf<RegionalDefectCycle>(item, item)).size, "views")
    }
}
