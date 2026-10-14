package com.acme.transport.network.zone.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newZoneClaim("code-334", "label-896")
        val view = toZoneClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toZoneClaimViews(mutableListOf<ZoneClaim>(item, item)).size, "views")
    }
}
