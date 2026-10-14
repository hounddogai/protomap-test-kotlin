package com.acme.transport.terminal.traileryard.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyTrailerYardReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyTrailerYardReservation("code-433", "name-658")
        val view = toLegacyTrailerYardReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyTrailerYardReservationViews(mutableListOf<LegacyTrailerYardReservation>(item, item)).size, "views")
    }
}
