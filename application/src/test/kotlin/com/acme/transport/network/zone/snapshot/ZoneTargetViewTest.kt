package com.acme.transport.network.zone.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newZoneTarget("code-218", "title-751")
        val view = toZoneTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toZoneTargetViews(mutableListOf<ZoneTarget>(item, item)).size, "views")
    }
}
