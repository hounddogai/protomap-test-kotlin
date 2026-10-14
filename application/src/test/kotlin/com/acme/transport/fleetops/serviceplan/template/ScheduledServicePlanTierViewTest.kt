package com.acme.transport.fleetops.serviceplan.template

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledServicePlanTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledServicePlanTier("code-520", "title-174")
        val view = toScheduledServicePlanTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledServicePlanTierViews(mutableListOf<ScheduledServicePlanTier>(item, item)).size, "views")
    }
}
