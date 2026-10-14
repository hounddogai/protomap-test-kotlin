package com.acme.transport.lastmile.attempt.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredAttemptMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredAttemptMetric("code-390", "label-947")
        val view = toTieredAttemptMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredAttemptMetricViews(mutableListOf<TieredAttemptMetric>(item, item)).size, "views")
    }
}
