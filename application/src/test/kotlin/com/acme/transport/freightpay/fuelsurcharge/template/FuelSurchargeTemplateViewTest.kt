package com.acme.transport.freightpay.fuelsurcharge.template

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelSurchargeTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelSurchargeTemplate("code-499", "label-678")
        val view = toFuelSurchargeTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelSurchargeTemplateViews(mutableListOf<FuelSurchargeTemplate>(item, item)).size, "views")
    }
}
