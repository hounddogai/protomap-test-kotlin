package com.acme.transport.safety.cameraevent.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackCameraEventAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackCameraEventAdjustment("code-566", "label-518")
        val view = toFallbackCameraEventAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackCameraEventAdjustmentViews(mutableListOf<FallbackCameraEventAdjustment>(item, item)).size, "views")
    }
}
