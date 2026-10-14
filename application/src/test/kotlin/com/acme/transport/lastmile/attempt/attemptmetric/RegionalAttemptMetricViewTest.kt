package com.acme.transport.lastmile.attempt.attemptmetric

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalAttemptMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalAttemptMetric("code-3", "title-728")
        val view = toRegionalAttemptMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalAttemptMetricViews(mutableListOf<RegionalAttemptMetric>(item, item)).size, "views")
    }
}
