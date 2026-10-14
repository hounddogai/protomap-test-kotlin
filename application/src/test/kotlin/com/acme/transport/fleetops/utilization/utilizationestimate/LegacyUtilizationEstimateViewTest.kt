package com.acme.transport.fleetops.utilization.utilizationestimate

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyUtilizationEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyUtilizationEstimate("code-5", "title-483")
        val view = toLegacyUtilizationEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyUtilizationEstimateViews(mutableListOf<LegacyUtilizationEstimate>(item, item)).size, "views")
    }
}
