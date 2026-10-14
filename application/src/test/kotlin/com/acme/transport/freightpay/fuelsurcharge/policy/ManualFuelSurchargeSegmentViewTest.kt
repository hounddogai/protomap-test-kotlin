package com.acme.transport.freightpay.fuelsurcharge.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualFuelSurchargeSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualFuelSurchargeSegment("code-409", "title-975")
        val view = toManualFuelSurchargeSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualFuelSurchargeSegmentViews(mutableListOf<ManualFuelSurchargeSegment>(item, item)).size, "views")
    }
}
