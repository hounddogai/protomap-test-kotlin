package com.acme.transport.network.cutoff.score

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientCutoffReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientCutoffReservation("code-852", "title-762")
        val view = toLenientCutoffReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientCutoffReservationViews(mutableListOf<LenientCutoffReservation>(item, item)).size, "views")
    }
}
