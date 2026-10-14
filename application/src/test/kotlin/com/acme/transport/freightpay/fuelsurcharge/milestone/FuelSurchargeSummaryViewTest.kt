package com.acme.transport.freightpay.fuelsurcharge.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelSurchargeSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelSurchargeSummary("code-789", "label-892")
        val view = toFuelSurchargeSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelSurchargeSummaryViews(mutableListOf<FuelSurchargeSummary>(item, item)).size, "views")
    }
}
