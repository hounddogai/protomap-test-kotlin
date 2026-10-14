package com.acme.transport.network.zone.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newZoneSlot("code-391", "title-981")
        val view = toZoneSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toZoneSlotViews(mutableListOf<ZoneSlot>(item, item)).size, "views")
    }
}
