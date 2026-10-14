package com.acme.transport.freightpay.fuelsurcharge.request

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelSurchargeRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelSurchargeRequest("code-65", "label-15")
        val view = toFuelSurchargeRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelSurchargeRequestViews(mutableListOf<FuelSurchargeRequest>(item, item)).size, "views")
    }
}
