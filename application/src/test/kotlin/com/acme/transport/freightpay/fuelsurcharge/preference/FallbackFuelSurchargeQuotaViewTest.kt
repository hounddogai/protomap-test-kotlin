package com.acme.transport.freightpay.fuelsurcharge.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackFuelSurchargeQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackFuelSurchargeQuota("code-245", "name-603")
        val view = toFallbackFuelSurchargeQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackFuelSurchargeQuotaViews(mutableListOf<FallbackFuelSurchargeQuota>(item, item)).size, "views")
    }
}
