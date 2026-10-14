package com.acme.transport.fleetops.utilization.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilizationReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newUtilizationReservation("code-145", "title-546")
        val view = toUtilizationReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toUtilizationReservationViews(mutableListOf<UtilizationReservation>(item, item)).size, "views")
    }
}
