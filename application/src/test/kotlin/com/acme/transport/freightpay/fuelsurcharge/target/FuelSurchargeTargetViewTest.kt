package com.acme.transport.freightpay.fuelsurcharge.target

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelSurchargeTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelSurchargeTarget("code-31", "name-727")
        val view = toFuelSurchargeTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelSurchargeTargetViews(mutableListOf<FuelSurchargeTarget>(item, item)).size, "views")
    }
}
