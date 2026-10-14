package com.acme.transport.fleetops.serviceplan.planrollup

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackServicePlanNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackServicePlanNotice("code-669", "title-771")
        val view = toFallbackServicePlanNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackServicePlanNoticeViews(mutableListOf<FallbackServicePlanNotice>(item, item)).size, "views")
    }
}
