package com.acme.transport.network.cutoff.globalcutoffledger

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredCutoffReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredCutoffReservation("code-969", "name-126")
        val view = toTieredCutoffReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredCutoffReservationViews(mutableListOf<TieredCutoffReservation>(item, item)).size, "views")
    }
}
