package com.acme.transport.network.zone.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newZoneTier("code-25", "label-784")
        val view = toZoneTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toZoneTierViews(mutableListOf<ZoneTier>(item, item)).size, "views")
    }
}
