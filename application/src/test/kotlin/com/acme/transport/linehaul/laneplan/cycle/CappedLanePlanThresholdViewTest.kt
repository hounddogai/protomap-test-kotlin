package com.acme.transport.linehaul.laneplan.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedLanePlanThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedLanePlanThreshold("code-507", "name-18")
        val view = toCappedLanePlanThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedLanePlanThresholdViews(mutableListOf<CappedLanePlanThreshold>(item, item)).size, "views")
    }
}
