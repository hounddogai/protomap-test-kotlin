package com.acme.transport.fleetops.inspection.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionReservation("code-61", "name-487")
        val view = toInspectionReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionReservationViews(mutableListOf<InspectionReservation>(item, item)).size, "views")
    }
}
