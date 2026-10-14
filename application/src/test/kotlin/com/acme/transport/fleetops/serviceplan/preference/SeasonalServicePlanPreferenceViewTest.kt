package com.acme.transport.fleetops.serviceplan.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalServicePlanPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalServicePlanPreference("code-77", "label-221")
        val view = toSeasonalServicePlanPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalServicePlanPreferenceViews(mutableListOf<SeasonalServicePlanPreference>(item, item)).size, "views")
    }
}
