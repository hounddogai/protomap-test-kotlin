package com.acme.transport.safety.cameraevent.eventaudit

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientCameraEventMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientCameraEventMetric("code-39", "title-116")
        val view = toLenientCameraEventMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientCameraEventMetricViews(mutableListOf<LenientCameraEventMetric>(item, item)).size, "views")
    }
}
