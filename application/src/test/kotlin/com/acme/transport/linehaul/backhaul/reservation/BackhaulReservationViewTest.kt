package com.acme.transport.linehaul.backhaul.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class BackhaulReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBackhaulReservation("code-362", "label-428")
        val view = toBackhaulReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBackhaulReservationViews(mutableListOf<BackhaulReservation>(item, item)).size, "views")
    }
}
