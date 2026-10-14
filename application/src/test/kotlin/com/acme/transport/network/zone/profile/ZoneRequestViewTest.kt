package com.acme.transport.network.zone.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newZoneRequest("code-648", "name-355")
        val view = toZoneRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toZoneRequestViews(mutableListOf<ZoneRequest>(item, item)).size, "views")
    }
}
