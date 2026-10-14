package com.acme.transport.freightpay.accessorial.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessorialAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAccessorialAdjustment("code-878", "title-881")
        val view = toAccessorialAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAccessorialAdjustmentViews(mutableListOf<AccessorialAdjustment>(item, item)).size, "views")
    }
}
