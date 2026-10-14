package com.acme.transport.safety.cameraevent.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraEventMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCameraEventMetric("code-649", "name-99")
        val view = toCameraEventMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCameraEventMetricViews(mutableListOf<CameraEventMetric>(item, item)).size, "views")
    }
}
