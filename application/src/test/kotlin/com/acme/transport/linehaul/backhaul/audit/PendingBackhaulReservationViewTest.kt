package com.acme.transport.linehaul.backhaul.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingBackhaulReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingBackhaulReservation("code-252", "name-263")
        val view = toPendingBackhaulReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingBackhaulReservationViews(mutableListOf<PendingBackhaulReservation>(item, item)).size, "views")
    }
}
