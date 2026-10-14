package com.acme.transport.linehaul.laneplan.template

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredLanePlanTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredLanePlanTemplate("code-792", "name-92")
        val view = toTieredLanePlanTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredLanePlanTemplateViews(mutableListOf<TieredLanePlanTemplate>(item, item)).size, "views")
    }
}
