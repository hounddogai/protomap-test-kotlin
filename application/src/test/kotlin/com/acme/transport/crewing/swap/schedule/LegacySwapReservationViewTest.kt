package com.acme.transport.crewing.swap.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacySwapReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacySwapReservation("code-84", "label-397")
        val view = toLegacySwapReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacySwapReservationViews(mutableListOf<LegacySwapReservation>(item, item)).size, "views")
    }
}
