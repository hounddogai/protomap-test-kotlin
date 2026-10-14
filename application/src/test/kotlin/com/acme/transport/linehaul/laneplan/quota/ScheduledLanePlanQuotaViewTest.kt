package com.acme.transport.linehaul.laneplan.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledLanePlanQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledLanePlanQuota("code-305", "label-264")
        val view = toScheduledLanePlanQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledLanePlanQuotaViews(mutableListOf<ScheduledLanePlanQuota>(item, item)).size, "views")
    }
}
