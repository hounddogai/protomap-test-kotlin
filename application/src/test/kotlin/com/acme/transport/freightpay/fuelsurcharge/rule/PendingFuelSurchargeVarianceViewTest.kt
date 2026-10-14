package com.acme.transport.freightpay.fuelsurcharge.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingFuelSurchargeVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingFuelSurchargeVariance("code-878", "title-168")
        val view = toPendingFuelSurchargeVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingFuelSurchargeVarianceViews(mutableListOf<PendingFuelSurchargeVariance>(item, item)).size, "views")
    }
}
