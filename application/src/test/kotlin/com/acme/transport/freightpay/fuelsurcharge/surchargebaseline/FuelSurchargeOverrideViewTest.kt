package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelSurchargeOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelSurchargeOverride("code-483", "title-741")
        val view = toFuelSurchargeOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelSurchargeOverrideViews(mutableListOf<FuelSurchargeOverride>(item, item)).size, "views")
    }
}
