package com.acme.transport.linehaul.laneplan.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class LanePlanTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLanePlanTemplate("code-8", "label-893")
        val view = toLanePlanTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLanePlanTemplateViews(mutableListOf<LanePlanTemplate>(item, item)).size, "views")
    }
}
