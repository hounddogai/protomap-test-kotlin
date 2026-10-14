package com.acme.transport.freightpay.fuelsurcharge.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingFuelSurchargeGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingFuelSurchargeGroup("code-287", "label-409")
        val view = toPendingFuelSurchargeGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingFuelSurchargeGroupViews(mutableListOf<PendingFuelSurchargeGroup>(item, item)).size, "views")
    }
}
