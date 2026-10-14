package com.acme.transport.freightpay.fuelsurcharge.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelSurchargeAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelSurchargeAllocation("code-157", "title-345")
        val view = toFuelSurchargeAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelSurchargeAllocationViews(mutableListOf<FuelSurchargeAllocation>(item, item)).size, "views")
    }
}
