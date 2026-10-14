package com.acme.transport.freightpay.dispute.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class DisputeReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDisputeReservation("code-827", "title-541")
        val view = toDisputeReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDisputeReservationViews(mutableListOf<DisputeReservation>(item, item)).size, "views")
    }
}
