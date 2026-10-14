package com.acme.transport.freightpay.fuelsurcharge.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelSurchargeGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelSurchargeGroup("code-662", "title-884")
        val view = toFuelSurchargeGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelSurchargeGroupViews(mutableListOf<FuelSurchargeGroup>(item, item)).size, "views")
    }
}
