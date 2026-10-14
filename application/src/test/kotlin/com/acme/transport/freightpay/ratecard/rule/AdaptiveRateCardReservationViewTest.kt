package com.acme.transport.freightpay.ratecard.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveRateCardReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveRateCardReservation("code-129", "name-869")
        val view = toAdaptiveRateCardReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveRateCardReservationViews(mutableListOf<AdaptiveRateCardReservation>(item, item)).size, "views")
    }
}
