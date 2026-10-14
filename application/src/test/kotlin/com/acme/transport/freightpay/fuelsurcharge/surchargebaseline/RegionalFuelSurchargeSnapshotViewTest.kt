package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalFuelSurchargeSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalFuelSurchargeSnapshot("code-853", "name-125")
        val view = toRegionalFuelSurchargeSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalFuelSurchargeSnapshotViews(mutableListOf<RegionalFuelSurchargeSnapshot>(item, item)).size, "views")
    }
}
