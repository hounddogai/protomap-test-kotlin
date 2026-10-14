package com.acme.transport.linehaul.laneplan.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class LanePlanCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLanePlanCycle("code-354", "label-744")
        val view = toLanePlanCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLanePlanCycleViews(mutableListOf<LanePlanCycle>(item, item)).size, "views")
    }
}
