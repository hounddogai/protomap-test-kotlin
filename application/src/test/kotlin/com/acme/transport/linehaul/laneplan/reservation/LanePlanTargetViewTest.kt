package com.acme.transport.linehaul.laneplan.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class LanePlanTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLanePlanTarget("code-790", "title-45")
        val view = toLanePlanTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLanePlanTargetViews(mutableListOf<LanePlanTarget>(item, item)).size, "views")
    }
}
