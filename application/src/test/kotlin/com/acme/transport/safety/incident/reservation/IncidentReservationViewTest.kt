package com.acme.transport.safety.incident.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class IncidentReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIncidentReservation("code-673", "title-773")
        val view = toIncidentReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIncidentReservationViews(mutableListOf<IncidentReservation>(item, item)).size, "views")
    }
}
