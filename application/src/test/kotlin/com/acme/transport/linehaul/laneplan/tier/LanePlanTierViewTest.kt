package com.acme.transport.linehaul.laneplan.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class LanePlanTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLanePlanTier("code-695", "name-714")
        val view = toLanePlanTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLanePlanTierViews(mutableListOf<LanePlanTier>(item, item)).size, "views")
    }
}
