package com.acme.transport.freightpay.fuelsurcharge.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalFuelSurchargeTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalFuelSurchargeTier("code-865", "label-486")
        val view = toSeasonalFuelSurchargeTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalFuelSurchargeTierViews(mutableListOf<SeasonalFuelSurchargeTier>(item, item)).size, "views")
    }
}
