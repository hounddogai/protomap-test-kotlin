package com.acme.transport.linehaul.laneplan.planreservation

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackLanePlanNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackLanePlanNotice("code-34", "title-767")
        val view = toFallbackLanePlanNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackLanePlanNoticeViews(mutableListOf<FallbackLanePlanNotice>(item, item)).size, "views")
    }
}
