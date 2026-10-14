package com.acme.transport.freightpay.fuelsurcharge.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelSurchargeSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelSurchargeSnapshot("code-226", "title-594")
        val view = toFuelSurchargeSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelSurchargeSnapshotViews(mutableListOf<FuelSurchargeSnapshot>(item, item)).size, "views")
    }
}
