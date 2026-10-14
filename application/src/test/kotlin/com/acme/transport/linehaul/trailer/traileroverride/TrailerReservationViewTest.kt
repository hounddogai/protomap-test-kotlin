package com.acme.transport.linehaul.trailer.traileroverride

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerReservation("code-780", "name-738")
        val view = toTrailerReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerReservationViews(mutableListOf<TrailerReservation>(item, item)).size, "views")
    }
}
