package com.acme.transport.crewing.roster.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class RosterReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRosterReservation("code-494", "title-878")
        val view = toRosterReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRosterReservationViews(mutableListOf<RosterReservation>(item, item)).size, "views")
    }
}
