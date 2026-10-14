package com.acme.transport.linehaul.laneplan.item

import kotlin.test.Test
import kotlin.test.assertEquals

class LanePlanAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLanePlanAdjustment("code-54", "name-67")
        val view = toLanePlanAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLanePlanAdjustmentViews(mutableListOf<LanePlanAdjustment>(item, item)).size, "views")
    }
}
