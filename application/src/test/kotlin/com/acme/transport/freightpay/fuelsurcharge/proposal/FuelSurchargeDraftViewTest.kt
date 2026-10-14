package com.acme.transport.freightpay.fuelsurcharge.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelSurchargeDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelSurchargeDraft("code-740", "label-143")
        val view = toFuelSurchargeDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelSurchargeDraftViews(mutableListOf<FuelSurchargeDraft>(item, item)).size, "views")
    }
}
