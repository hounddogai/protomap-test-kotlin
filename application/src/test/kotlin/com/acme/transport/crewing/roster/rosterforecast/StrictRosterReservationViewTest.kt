package com.acme.transport.crewing.roster.rosterforecast

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictRosterReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictRosterReservation("code-625", "name-303")
        val view = toStrictRosterReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictRosterReservationViews(mutableListOf<StrictRosterReservation>(item, item)).size, "views")
    }
}
