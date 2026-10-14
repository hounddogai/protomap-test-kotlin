package com.acme.transport.freightpay.fuelsurcharge.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelSurchargeEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelSurchargeEntry("code-772", "title-498")
        val view = toFuelSurchargeEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelSurchargeEntryViews(mutableListOf<FuelSurchargeEntry>(item, item)).size, "views")
    }
}
