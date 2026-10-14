package com.acme.transport.fleetops.serviceplan.legacyplanquota

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedServicePlanMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedServicePlanMilestone("code-838", "label-22")
        val view = toWeightedServicePlanMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedServicePlanMilestoneViews(mutableListOf<WeightedServicePlanMilestone>(item, item)).size, "views")
    }
}
