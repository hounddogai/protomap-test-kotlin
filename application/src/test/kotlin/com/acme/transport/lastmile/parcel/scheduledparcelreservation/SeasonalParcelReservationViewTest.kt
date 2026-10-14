package com.acme.transport.lastmile.parcel.scheduledparcelreservation

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalParcelReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalParcelReservation("code-97", "label-795")
        val view = toSeasonalParcelReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalParcelReservationViews(mutableListOf<SeasonalParcelReservation>(item, item)).size, "views")
    }
}
