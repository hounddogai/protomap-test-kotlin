package com.acme.transport.lastmile.attempt.attemptpolicy

import kotlin.test.Test
import kotlin.test.assertEquals

class AttemptMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAttemptMetric("code-753", "label-802")
        val view = toAttemptMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAttemptMetricViews(mutableListOf<AttemptMetric>(item, item)).size, "views")
    }
}
