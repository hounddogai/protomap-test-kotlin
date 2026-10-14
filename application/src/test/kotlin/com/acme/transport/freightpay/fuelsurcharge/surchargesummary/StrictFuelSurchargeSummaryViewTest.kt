package com.acme.transport.freightpay.fuelsurcharge.surchargesummary

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictFuelSurchargeSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictFuelSurchargeSummary("code-839", "title-919")
        val view = toStrictFuelSurchargeSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictFuelSurchargeSummaryViews(mutableListOf<StrictFuelSurchargeSummary>(item, item)).size, "views")
    }
}
