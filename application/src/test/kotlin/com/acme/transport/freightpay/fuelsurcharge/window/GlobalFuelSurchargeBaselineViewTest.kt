package com.acme.transport.freightpay.fuelsurcharge.window

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalFuelSurchargeBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalFuelSurchargeBaseline("code-689", "title-449")
        val view = toGlobalFuelSurchargeBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalFuelSurchargeBaselineViews(mutableListOf<GlobalFuelSurchargeBaseline>(item, item)).size, "views")
    }
}
