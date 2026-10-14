package com.acme.transport.safety.cameraevent.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictCameraEventMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictCameraEventMetric("code-254", "title-29")
        val view = toStrictCameraEventMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictCameraEventMetricViews(mutableListOf<StrictCameraEventMetric>(item, item)).size, "views")
    }
}
