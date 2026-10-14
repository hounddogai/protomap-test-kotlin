package com.acme.transport.freightpay.fuelsurcharge.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelSurchargeBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelSurchargeBaseline("code-843", "title-576")
        val view = toFuelSurchargeBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelSurchargeBaselineViews(mutableListOf<FuelSurchargeBaseline>(item, item)).size, "views")
    }
}
