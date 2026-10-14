package com.acme.transport.fleetops.tire.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingTireReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingTireReservation("code-726", "title-950")
        val view = toRollingTireReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingTireReservationViews(mutableListOf<RollingTireReservation>(item, item)).size, "views")
    }
}
