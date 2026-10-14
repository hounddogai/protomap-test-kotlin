package com.acme.transport.linehaul.laneplan.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredLanePlanRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredLanePlanRequest("code-835", "label-22")
        val view = toTieredLanePlanRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredLanePlanRequestViews(mutableListOf<TieredLanePlanRequest>(item, item)).size, "views")
    }
}
