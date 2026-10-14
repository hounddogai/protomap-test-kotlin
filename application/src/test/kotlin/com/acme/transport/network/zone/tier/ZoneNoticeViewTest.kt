package com.acme.transport.network.zone.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newZoneNotice("code-373", "title-920")
        val view = toZoneNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toZoneNoticeViews(mutableListOf<ZoneNotice>(item, item)).size, "views")
    }
}
