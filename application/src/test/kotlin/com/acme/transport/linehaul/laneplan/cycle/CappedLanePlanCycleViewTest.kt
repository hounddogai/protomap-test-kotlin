package com.acme.transport.linehaul.laneplan.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedLanePlanCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedLanePlanCycle("code-853", "title-313")
        val view = toCappedLanePlanCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedLanePlanCycleViews(mutableListOf<CappedLanePlanCycle>(item, item)).size, "views")
    }
}
