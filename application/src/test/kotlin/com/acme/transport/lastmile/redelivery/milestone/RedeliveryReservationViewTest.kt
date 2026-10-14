package com.acme.transport.lastmile.redelivery.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class RedeliveryReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRedeliveryReservation("code-366", "name-894")
        val view = toRedeliveryReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRedeliveryReservationViews(mutableListOf<RedeliveryReservation>(item, item)).size, "views")
    }
}
