package com.acme.transport.fleetops.serviceplan.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedServicePlanEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedServicePlanEntry("code-581", "name-383")
        val view = toWeightedServicePlanEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedServicePlanEntryViews(mutableListOf<WeightedServicePlanEntry>(item, item)).size, "views")
    }
}
