package com.acme.transport.safety.nearmiss.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class NearMissMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNearMissMetric("code-587", "label-827")
        val view = toNearMissMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNearMissMetricViews(mutableListOf<NearMissMetric>(item, item)).size, "views")
    }
}
