package com.acme.transport.safety.nearmiss.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultNearMissMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultNearMissMetric("code-721", "title-96")
        val view = toDefaultNearMissMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultNearMissMetricViews(mutableListOf<DefaultNearMissMetric>(item, item)).size, "views")
    }
}
