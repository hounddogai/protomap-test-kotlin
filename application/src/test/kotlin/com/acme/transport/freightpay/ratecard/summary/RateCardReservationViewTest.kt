package com.acme.transport.freightpay.ratecard.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class RateCardReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRateCardReservation("code-112", "name-255")
        val view = toRateCardReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRateCardReservationViews(mutableListOf<RateCardReservation>(item, item)).size, "views")
    }
}
