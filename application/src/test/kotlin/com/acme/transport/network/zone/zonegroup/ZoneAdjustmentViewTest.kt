package com.acme.transport.network.zone.zonegroup

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newZoneAdjustment("code-840", "title-78")
        val view = toZoneAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toZoneAdjustmentViews(mutableListOf<ZoneAdjustment>(item, item)).size, "views")
    }
}
