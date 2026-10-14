package com.acme.transport.freightpay.fuelsurcharge.surchargerule

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelSurchargeScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelSurchargeScore("code-423", "name-166")
        val view = toFuelSurchargeScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelSurchargeScoreViews(mutableListOf<FuelSurchargeScore>(item, item)).size, "views")
    }
}
