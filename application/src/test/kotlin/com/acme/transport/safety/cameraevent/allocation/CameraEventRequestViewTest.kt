package com.acme.transport.safety.cameraevent.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraEventRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCameraEventRequest("code-760", "label-540")
        val view = toCameraEventRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCameraEventRequestViews(mutableListOf<CameraEventRequest>(item, item)).size, "views")
    }
}
