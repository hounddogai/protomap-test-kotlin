package com.acme.transport.linehaul.trailer.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledTrailerMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledTrailerMetric("code-713", "title-928")
        val view = toScheduledTrailerMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledTrailerMetricViews(mutableListOf<ScheduledTrailerMetric>(item, item)).size, "views")
    }
}
