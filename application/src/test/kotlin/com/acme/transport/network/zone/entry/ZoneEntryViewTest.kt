package com.acme.transport.network.zone.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newZoneEntry("code-534", "title-154")
        val view = toZoneEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toZoneEntryViews(mutableListOf<ZoneEntry>(item, item)).size, "views")
    }
}
