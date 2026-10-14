package com.acme.transport.network.cutoff.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class CutoffReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCutoffReservation("code-694", "label-370")
        val view = toCutoffReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCutoffReservationViews(mutableListOf<CutoffReservation>(item, item)).size, "views")
    }
}
