package com.acme.transport.network.zone.group

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedZoneReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedZoneReservation("code-730", "label-295")
        val view = toCappedZoneReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedZoneReservationViews(mutableListOf<CappedZoneReservation>(item, item)).size, "views")
    }
}
