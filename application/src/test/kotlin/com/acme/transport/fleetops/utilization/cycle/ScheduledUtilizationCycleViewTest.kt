package com.acme.transport.fleetops.utilization.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledUtilizationCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledUtilizationCycle("code-620", "name-559")
        val view = toScheduledUtilizationCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledUtilizationCycleViews(mutableListOf<ScheduledUtilizationCycle>(item, item)).size, "views")
    }
}
