package com.acme.transport.safety.cameraevent.eventrecord

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackCameraEventSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackCameraEventSummary("code-185", "label-74")
        val view = toFallbackCameraEventSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackCameraEventSummaryViews(mutableListOf<FallbackCameraEventSummary>(item, item)).size, "views")
    }
}
