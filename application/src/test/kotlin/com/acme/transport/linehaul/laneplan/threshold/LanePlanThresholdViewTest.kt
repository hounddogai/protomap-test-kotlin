package com.acme.transport.linehaul.laneplan.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class LanePlanThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLanePlanThreshold("code-327", "title-909")
        val view = toLanePlanThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLanePlanThresholdViews(mutableListOf<LanePlanThreshold>(item, item)).size, "views")
    }
}
