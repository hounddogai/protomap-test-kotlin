package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingUtilizationScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingUtilizationScore("code-692", "name-655")
        val view = toRollingUtilizationScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingUtilizationScoreViews(mutableListOf<RollingUtilizationScore>(item, item)).size, "views")
    }
}
