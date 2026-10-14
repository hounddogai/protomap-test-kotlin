package com.acme.transport.network.zone.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newZoneReservation("code-193", "label-757")
        val view = toZoneReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toZoneReservationViews(mutableListOf<ZoneReservation>(item, item)).size, "views")
    }
}
