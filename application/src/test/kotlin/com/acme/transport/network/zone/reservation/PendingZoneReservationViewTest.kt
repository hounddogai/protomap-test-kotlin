package com.acme.transport.network.zone.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingZoneReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingZoneReservation("code-631", "label-895")
        val view = toPendingZoneReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingZoneReservationViews(mutableListOf<PendingZoneReservation>(item, item)).size, "views")
    }
}
