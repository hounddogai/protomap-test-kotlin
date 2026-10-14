package com.acme.transport.customs.broker.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyBrokerReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyBrokerReservation("code-760", "label-701")
        val view = toLegacyBrokerReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyBrokerReservationViews(mutableListOf<LegacyBrokerReservation>(item, item)).size, "views")
    }
}
