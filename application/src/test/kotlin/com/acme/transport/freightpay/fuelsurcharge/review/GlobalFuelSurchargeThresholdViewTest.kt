package com.acme.transport.freightpay.fuelsurcharge.review

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalFuelSurchargeThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalFuelSurchargeThreshold("code-896", "title-42")
        val view = toGlobalFuelSurchargeThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalFuelSurchargeThresholdViews(mutableListOf<GlobalFuelSurchargeThreshold>(item, item)).size, "views")
    }
}
