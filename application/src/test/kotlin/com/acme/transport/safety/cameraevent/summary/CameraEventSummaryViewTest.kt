package com.acme.transport.safety.cameraevent.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraEventSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCameraEventSummary("code-285", "title-798")
        val view = toCameraEventSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCameraEventSummaryViews(mutableListOf<CameraEventSummary>(item, item)).size, "views")
    }
}
