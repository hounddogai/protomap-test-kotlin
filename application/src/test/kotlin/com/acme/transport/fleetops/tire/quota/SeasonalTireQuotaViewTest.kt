package com.acme.transport.fleetops.tire.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalTireQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalTireQuota("code-608", "name-247")
        val view = toSeasonalTireQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalTireQuotaViews(mutableListOf<SeasonalTireQuota>(item, item)).size, "views")
    }
}
