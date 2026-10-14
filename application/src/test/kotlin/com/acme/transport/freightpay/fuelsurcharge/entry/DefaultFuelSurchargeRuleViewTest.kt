package com.acme.transport.freightpay.fuelsurcharge.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultFuelSurchargeRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultFuelSurchargeRule("code-690", "label-870")
        val view = toDefaultFuelSurchargeRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultFuelSurchargeRuleViews(mutableListOf<DefaultFuelSurchargeRule>(item, item)).size, "views")
    }
}
