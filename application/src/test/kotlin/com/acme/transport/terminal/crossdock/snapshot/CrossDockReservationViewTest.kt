package com.acme.transport.terminal.crossdock.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class CrossDockReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCrossDockReservation("code-4", "title-581")
        val view = toCrossDockReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCrossDockReservationViews(mutableListOf<CrossDockReservation>(item, item)).size, "views")
    }
}
