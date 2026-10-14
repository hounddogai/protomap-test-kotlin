package com.acme.transport.freightpay.fuelsurcharge.window

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelSurchargeMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelSurchargeMilestone("code-604", "label-989")
        val view = toFuelSurchargeMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelSurchargeMilestoneViews(mutableListOf<FuelSurchargeMilestone>(item, item)).size, "views")
    }
}
