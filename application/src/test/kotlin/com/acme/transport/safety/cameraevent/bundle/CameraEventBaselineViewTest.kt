package com.acme.transport.safety.cameraevent.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraEventBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCameraEventBaseline("code-569", "label-241")
        val view = toCameraEventBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCameraEventBaselineViews(mutableListOf<CameraEventBaseline>(item, item)).size, "views")
    }
}
