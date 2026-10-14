package com.acme.transport.network.zone.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newZoneRollup("code-532", "label-643")
        val view = toZoneRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toZoneRollupViews(mutableListOf<ZoneRollup>(item, item)).size, "views")
    }
}
