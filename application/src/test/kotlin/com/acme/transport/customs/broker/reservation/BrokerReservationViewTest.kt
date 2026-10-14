package com.acme.transport.customs.broker.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerReservation("code-462", "title-941")
        val view = toBrokerReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerReservationViews(mutableListOf<BrokerReservation>(item, item)).size, "views")
    }
}
