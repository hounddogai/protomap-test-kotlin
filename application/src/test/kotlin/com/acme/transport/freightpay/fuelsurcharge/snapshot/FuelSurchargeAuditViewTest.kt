package com.acme.transport.freightpay.fuelsurcharge.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelSurchargeAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelSurchargeAudit("code-761", "title-913")
        val view = toFuelSurchargeAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelSurchargeAuditViews(mutableListOf<FuelSurchargeAudit>(item, item)).size, "views")
    }
}
