package com.acme.transport.freightpay.fuelsurcharge.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledFuelSurchargeBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledFuelSurchargeBundle("code-901", "name-583")
        val view = toScheduledFuelSurchargeBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledFuelSurchargeBundleViews(mutableListOf<ScheduledFuelSurchargeBundle>(item, item)).size, "views")
    }
}
