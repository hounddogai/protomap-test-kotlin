package com.acme.transport.freightpay.fuelsurcharge.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualFuelSurchargeMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualFuelSurchargeMilestone("code-934", "title-933")
        val view = toManualFuelSurchargeMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualFuelSurchargeMilestoneViews(mutableListOf<ManualFuelSurchargeMilestone>(item, item)).size, "views")
    }
}
