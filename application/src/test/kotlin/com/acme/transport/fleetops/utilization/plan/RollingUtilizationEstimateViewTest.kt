package com.acme.transport.fleetops.utilization.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingUtilizationEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingUtilizationEstimate("code-559", "name-188")
        val view = toRollingUtilizationEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingUtilizationEstimateViews(mutableListOf<RollingUtilizationEstimate>(item, item)).size, "views")
    }
}
