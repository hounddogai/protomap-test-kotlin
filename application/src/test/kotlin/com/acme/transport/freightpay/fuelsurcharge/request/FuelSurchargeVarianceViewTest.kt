package com.acme.transport.freightpay.fuelsurcharge.request

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelSurchargeVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelSurchargeVariance("code-122", "label-610")
        val view = toFuelSurchargeVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelSurchargeVarianceViews(mutableListOf<FuelSurchargeVariance>(item, item)).size, "views")
    }
}
