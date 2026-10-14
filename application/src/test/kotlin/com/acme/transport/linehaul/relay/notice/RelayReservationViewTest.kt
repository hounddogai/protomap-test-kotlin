package com.acme.transport.linehaul.relay.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class RelayReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRelayReservation("code-327", "name-768")
        val view = toRelayReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRelayReservationViews(mutableListOf<RelayReservation>(item, item)).size, "views")
    }
}
