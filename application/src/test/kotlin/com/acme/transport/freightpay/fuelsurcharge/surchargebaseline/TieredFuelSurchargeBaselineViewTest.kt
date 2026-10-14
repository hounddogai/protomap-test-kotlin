package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredFuelSurchargeBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredFuelSurchargeBaseline("code-230", "label-561")
        val view = toTieredFuelSurchargeBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredFuelSurchargeBaselineViews(mutableListOf<TieredFuelSurchargeBaseline>(item, item)).size, "views")
    }
}
