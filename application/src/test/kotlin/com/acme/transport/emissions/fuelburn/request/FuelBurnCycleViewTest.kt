package com.acme.transport.emissions.fuelburn.request

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnCycle("code-535", "name-510")
        val view = toFuelBurnCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnCycleViews(mutableListOf<FuelBurnCycle>(item, item)).size, "views")
    }
}
