package com.acme.transport.linehaul.capacity.capacityreservation

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredCapacityReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredCapacityReservation("code-821", "label-500")
        val view = toTieredCapacityReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredCapacityReservationViews(mutableListOf<TieredCapacityReservation>(item, item)).size, "views")
    }
}
