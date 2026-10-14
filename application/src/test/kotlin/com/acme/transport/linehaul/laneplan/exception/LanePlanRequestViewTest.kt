package com.acme.transport.linehaul.laneplan.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class LanePlanRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLanePlanRequest("code-125", "label-467")
        val view = toLanePlanRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLanePlanRequestViews(mutableListOf<LanePlanRequest>(item, item)).size, "views")
    }
}
