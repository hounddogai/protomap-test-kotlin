package com.acme.transport.freightpay.fuelsurcharge.surchargeslot

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalFuelSurchargeSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalFuelSurchargeSlot("code-757", "title-272")
        val view = toSeasonalFuelSurchargeSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalFuelSurchargeSlotViews(mutableListOf<SeasonalFuelSurchargeSlot>(item, item)).size, "views")
    }
}
