package com.acme.transport.freightpay.fuelsurcharge.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalFuelSurchargeGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalFuelSurchargeGroup("code-844", "label-229")
        val view = toRegionalFuelSurchargeGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalFuelSurchargeGroupViews(mutableListOf<RegionalFuelSurchargeGroup>(item, item)).size, "views")
    }
}
