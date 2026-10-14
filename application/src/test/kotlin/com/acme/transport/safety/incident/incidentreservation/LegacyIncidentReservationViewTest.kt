package com.acme.transport.safety.incident.incidentreservation

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyIncidentReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyIncidentReservation("code-724", "title-462")
        val view = toLegacyIncidentReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyIncidentReservationViews(mutableListOf<LegacyIncidentReservation>(item, item)).size, "views")
    }
}
