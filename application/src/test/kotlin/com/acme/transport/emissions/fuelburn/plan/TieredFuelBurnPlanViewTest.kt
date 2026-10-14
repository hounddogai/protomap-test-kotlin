package com.acme.transport.emissions.fuelburn.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredFuelBurnPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredFuelBurnPlan("code-615", "title-750")
        val view = toTieredFuelBurnPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredFuelBurnPlanViews(mutableListOf<TieredFuelBurnPlan>(item, item)).size, "views")
    }
}
