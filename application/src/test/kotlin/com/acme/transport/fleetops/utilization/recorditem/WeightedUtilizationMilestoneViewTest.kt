package com.acme.transport.fleetops.utilization.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedUtilizationMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedUtilizationMilestone("code-510", "title-106")
        val view = toWeightedUtilizationMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedUtilizationMilestoneViews(mutableListOf<WeightedUtilizationMilestone>(item, item)).size, "views")
    }
}
