package com.acme.transport.network.zone.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredZoneReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredZoneReservation("code-197", "title-461")
        val view = toTieredZoneReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredZoneReservationViews(mutableListOf<TieredZoneReservation>(item, item)).size, "views")
    }
}
