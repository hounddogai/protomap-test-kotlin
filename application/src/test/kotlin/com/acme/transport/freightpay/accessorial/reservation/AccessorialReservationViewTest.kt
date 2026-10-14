package com.acme.transport.freightpay.accessorial.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessorialReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAccessorialReservation("code-197", "title-584")
        val view = toAccessorialReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAccessorialReservationViews(mutableListOf<AccessorialReservation>(item, item)).size, "views")
    }
}
