package com.acme.transport.linehaul.laneplan.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class LanePlanMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLanePlanMapping("code-578", "label-820")
        val view = toLanePlanMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLanePlanMappingViews(mutableListOf<LanePlanMapping>(item, item)).size, "views")
    }
}
