package com.acme.transport.linehaul.laneplan.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class LanePlanOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLanePlanOverride("code-230", "label-21")
        val view = toLanePlanOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLanePlanOverrideViews(mutableListOf<LanePlanOverride>(item, item)).size, "views")
    }
}
