package com.acme.transport.safety.cameraevent.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraEventEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCameraEventEstimate("code-105", "name-558")
        val view = toCameraEventEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCameraEventEstimateViews(mutableListOf<CameraEventEstimate>(item, item)).size, "views")
    }
}
