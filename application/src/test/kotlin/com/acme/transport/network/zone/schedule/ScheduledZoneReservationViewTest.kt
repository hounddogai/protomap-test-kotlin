package com.acme.transport.network.zone.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledZoneReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledZoneReservation("code-440", "label-478")
        val view = toScheduledZoneReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledZoneReservationViews(mutableListOf<ScheduledZoneReservation>(item, item)).size, "views")
    }
}
