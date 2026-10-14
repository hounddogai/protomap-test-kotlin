package com.acme.transport.freightpay.fuelsurcharge.request

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelSurchargeAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelSurchargeAdjustment("code-423", "name-642")
        val view = toFuelSurchargeAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelSurchargeAdjustmentViews(mutableListOf<FuelSurchargeAdjustment>(item, item)).size, "views")
    }
}
