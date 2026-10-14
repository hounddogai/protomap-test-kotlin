package com.acme.transport.crewing.availability.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class AvailabilityReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAvailabilityReservation("code-512", "label-175")
        val view = toAvailabilityReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAvailabilityReservationViews(mutableListOf<AvailabilityReservation>(item, item)).size, "views")
    }
}
