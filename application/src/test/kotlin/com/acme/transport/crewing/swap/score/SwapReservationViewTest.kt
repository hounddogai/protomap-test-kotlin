package com.acme.transport.crewing.swap.score

import kotlin.test.Test
import kotlin.test.assertEquals

class SwapReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSwapReservation("code-763", "label-637")
        val view = toSwapReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSwapReservationViews(mutableListOf<SwapReservation>(item, item)).size, "views")
    }
}
