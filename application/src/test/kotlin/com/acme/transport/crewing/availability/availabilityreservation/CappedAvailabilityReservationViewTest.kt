package com.acme.transport.crewing.availability.availabilityreservation

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedAvailabilityReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedAvailabilityReservation("code-965", "name-64")
        val view = toCappedAvailabilityReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedAvailabilityReservationViews(mutableListOf<CappedAvailabilityReservation>(item, item)).size, "views")
    }
}
