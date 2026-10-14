package com.acme.transport.freightpay.fuelsurcharge.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelSurchargeCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelSurchargeCycle("code-163", "name-657")
        val view = toFuelSurchargeCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelSurchargeCycleViews(mutableListOf<FuelSurchargeCycle>(item, item)).size, "views")
    }
}
