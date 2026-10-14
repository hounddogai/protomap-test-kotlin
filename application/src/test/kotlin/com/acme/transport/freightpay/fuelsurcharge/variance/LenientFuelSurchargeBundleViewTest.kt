package com.acme.transport.freightpay.fuelsurcharge.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientFuelSurchargeBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientFuelSurchargeBundle("code-634", "title-318")
        val view = toLenientFuelSurchargeBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientFuelSurchargeBundleViews(mutableListOf<LenientFuelSurchargeBundle>(item, item)).size, "views")
    }
}
