package com.acme.transport.linehaul.laneplan.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class LanePlanNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLanePlanNotice("code-505", "name-556")
        val view = toLanePlanNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLanePlanNoticeViews(mutableListOf<LanePlanNotice>(item, item)).size, "views")
    }
}
