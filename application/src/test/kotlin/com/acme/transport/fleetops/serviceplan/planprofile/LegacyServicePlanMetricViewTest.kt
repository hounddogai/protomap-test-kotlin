package com.acme.transport.fleetops.serviceplan.planprofile

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyServicePlanMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyServicePlanMetric("code-421", "label-206")
        val view = toLegacyServicePlanMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyServicePlanMetricViews(mutableListOf<LegacyServicePlanMetric>(item, item)).size, "views")
    }
}
