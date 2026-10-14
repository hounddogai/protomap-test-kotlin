package com.acme.transport.linehaul.laneplan.request

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyLanePlanQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyLanePlanQuota("code-329", "title-877")
        val view = toLegacyLanePlanQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyLanePlanQuotaViews(mutableListOf<LegacyLanePlanQuota>(item, item)).size, "views")
    }
}
