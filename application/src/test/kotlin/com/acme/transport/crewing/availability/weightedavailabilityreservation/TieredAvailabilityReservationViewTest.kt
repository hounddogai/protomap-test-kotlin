package com.acme.transport.crewing.availability.weightedavailabilityreservation

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredAvailabilityReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredAvailabilityReservation("code-213", "title-182")
        val view = toTieredAvailabilityReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredAvailabilityReservationViews(mutableListOf<TieredAvailabilityReservation>(item, item)).size, "views")
    }
}
