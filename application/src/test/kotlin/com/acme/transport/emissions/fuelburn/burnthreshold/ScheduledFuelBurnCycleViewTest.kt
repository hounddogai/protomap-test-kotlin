package com.acme.transport.emissions.fuelburn.burnthreshold

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledFuelBurnCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledFuelBurnCycle("code-101", "name-697")
        val view = toScheduledFuelBurnCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledFuelBurnCycleViews(mutableListOf<ScheduledFuelBurnCycle>(item, item)).size, "views")
    }
}
