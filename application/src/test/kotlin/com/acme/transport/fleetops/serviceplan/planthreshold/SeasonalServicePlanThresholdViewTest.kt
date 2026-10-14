package com.acme.transport.fleetops.serviceplan.planthreshold

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalServicePlanThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalServicePlanThreshold("code-169", "label-790")
        val view = toSeasonalServicePlanThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalServicePlanThresholdViews(mutableListOf<SeasonalServicePlanThreshold>(item, item)).size, "views")
    }
}
