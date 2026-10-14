package com.acme.transport.linehaul.laneplan.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class LanePlanWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLanePlanWindow("code-207", "name-966")
        val view = toLanePlanWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLanePlanWindowViews(mutableListOf<LanePlanWindow>(item, item)).size, "views")
    }
}
