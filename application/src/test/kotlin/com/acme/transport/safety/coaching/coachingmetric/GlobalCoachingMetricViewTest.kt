package com.acme.transport.safety.coaching.coachingmetric

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalCoachingMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalCoachingMetric("code-34", "name-316")
        val view = toGlobalCoachingMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalCoachingMetricViews(mutableListOf<GlobalCoachingMetric>(item, item)).size, "views")
    }
}
