package com.acme.transport.freightpay.dispute.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientDisputeReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientDisputeReservation("code-569", "name-366")
        val view = toLenientDisputeReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientDisputeReservationViews(mutableListOf<LenientDisputeReservation>(item, item)).size, "views")
    }
}
