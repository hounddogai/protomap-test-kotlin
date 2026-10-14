package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalFuelSurchargeAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalFuelSurchargeAdjustment("code-882", "title-446")
        val view = toSeasonalFuelSurchargeAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalFuelSurchargeAdjustmentViews(mutableListOf<SeasonalFuelSurchargeAdjustment>(item, item)).size, "views")
    }
}
