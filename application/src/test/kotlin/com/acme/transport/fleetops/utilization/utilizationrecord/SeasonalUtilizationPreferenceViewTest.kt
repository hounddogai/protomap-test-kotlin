package com.acme.transport.fleetops.utilization.utilizationrecord

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalUtilizationPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalUtilizationPreference("code-660", "name-468")
        val view = toSeasonalUtilizationPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalUtilizationPreferenceViews(mutableListOf<SeasonalUtilizationPreference>(item, item)).size, "views")
    }
}
