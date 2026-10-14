package com.acme.transport.safety.nearmiss.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class NearMissReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNearMissReservation("code-110", "label-503")
        val view = toNearMissReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNearMissReservationViews(mutableListOf<NearMissReservation>(item, item)).size, "views")
    }
}
