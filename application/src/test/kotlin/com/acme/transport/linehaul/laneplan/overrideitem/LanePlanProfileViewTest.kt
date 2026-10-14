package com.acme.transport.linehaul.laneplan.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class LanePlanProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLanePlanProfile("code-296", "label-924")
        val view = toLanePlanProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLanePlanProfileViews(mutableListOf<LanePlanProfile>(item, item)).size, "views")
    }
}
