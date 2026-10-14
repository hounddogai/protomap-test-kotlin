package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelSurchargeClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelSurchargeClaim("code-747", "title-616")
        val view = toFuelSurchargeClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelSurchargeClaimViews(mutableListOf<FuelSurchargeClaim>(item, item)).size, "views")
    }
}
