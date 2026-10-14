package com.acme.transport.safety.cameraevent.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientCameraEventAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientCameraEventAdjustment("code-56", "label-801")
        val view = toLenientCameraEventAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientCameraEventAdjustmentViews(mutableListOf<LenientCameraEventAdjustment>(item, item)).size, "views")
    }
}
