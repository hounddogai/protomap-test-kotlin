package com.acme.transport.fleetops.utilization.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilizationThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newUtilizationThreshold("code-700", "name-757")
        val view = toUtilizationThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toUtilizationThresholdViews(mutableListOf<UtilizationThreshold>(item, item)).size, "views")
    }
}
