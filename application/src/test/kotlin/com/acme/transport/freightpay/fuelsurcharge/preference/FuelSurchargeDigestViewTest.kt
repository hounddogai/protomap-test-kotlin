package com.acme.transport.freightpay.fuelsurcharge.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelSurchargeDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelSurchargeDigest("code-72", "title-675")
        val view = toFuelSurchargeDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelSurchargeDigestViews(mutableListOf<FuelSurchargeDigest>(item, item)).size, "views")
    }
}
