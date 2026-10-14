package com.acme.transport.freightpay.fuelsurcharge.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelSurchargeReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelSurchargeReservation("code-730", "name-637")
        val view = toFuelSurchargeReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelSurchargeReservationViews(mutableListOf<FuelSurchargeReservation>(item, item)).size, "views")
    }
}
