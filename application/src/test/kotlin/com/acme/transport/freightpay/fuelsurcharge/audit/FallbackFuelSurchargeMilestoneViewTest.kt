package com.acme.transport.freightpay.fuelsurcharge.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackFuelSurchargeMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackFuelSurchargeMilestone("code-764", "title-52")
        val view = toFallbackFuelSurchargeMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackFuelSurchargeMilestoneViews(mutableListOf<FallbackFuelSurchargeMilestone>(item, item)).size, "views")
    }
}
