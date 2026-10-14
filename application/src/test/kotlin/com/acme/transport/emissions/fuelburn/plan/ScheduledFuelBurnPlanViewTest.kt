package com.acme.transport.emissions.fuelburn.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledFuelBurnPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledFuelBurnPlan("code-722", "label-835")
        val view = toScheduledFuelBurnPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledFuelBurnPlanViews(mutableListOf<ScheduledFuelBurnPlan>(item, item)).size, "views")
    }
}
