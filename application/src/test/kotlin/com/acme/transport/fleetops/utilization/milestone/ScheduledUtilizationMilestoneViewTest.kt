package com.acme.transport.fleetops.utilization.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledUtilizationMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledUtilizationMilestone("code-997", "name-630")
        val view = toScheduledUtilizationMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledUtilizationMilestoneViews(mutableListOf<ScheduledUtilizationMilestone>(item, item)).size, "views")
    }
}
