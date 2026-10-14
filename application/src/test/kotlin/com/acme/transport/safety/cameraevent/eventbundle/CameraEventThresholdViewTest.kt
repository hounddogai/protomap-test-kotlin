package com.acme.transport.safety.cameraevent.eventbundle

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraEventThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCameraEventThreshold("code-450", "label-443")
        val view = toCameraEventThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCameraEventThresholdViews(mutableListOf<CameraEventThreshold>(item, item)).size, "views")
    }
}
