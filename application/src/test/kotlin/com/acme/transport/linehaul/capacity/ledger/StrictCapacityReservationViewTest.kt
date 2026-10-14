package com.acme.transport.linehaul.capacity.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictCapacityReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictCapacityReservation("code-424", "name-199")
        val view = toStrictCapacityReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictCapacityReservationViews(mutableListOf<StrictCapacityReservation>(item, item)).size, "views")
    }
}
