package com.acme.transport.customs.hold.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryHoldAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryHoldAdjustment("code-141", "name-59")
        val view = toPrimaryHoldAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryHoldAdjustmentViews(mutableListOf<PrimaryHoldAdjustment>(item, item)).size, "views")
    }
}
